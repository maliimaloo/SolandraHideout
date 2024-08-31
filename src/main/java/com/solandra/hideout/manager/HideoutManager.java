package com.solandra.hideout.manager;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.SideEffectSet;
import com.solandra.hideout.Main;
import com.solandra.hideout.database.HideoutDatabase;
import com.solandra.hideout.manager.builder.HideoutBuilder;
import com.solandra.hideout.model.Hideout;
import com.solandra.hideout.utils.Schematic;
import org.bukkit.World;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.region.Region;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Gère les opérations liées aux hideouts, telles que le chargement, la création, la mise à jour,
 * et l'application de schématiques dans le jeu.
 */
public class HideoutManager {

    // Messages constants
    private static final String ERROR_PLACE_SCHEMATIC = "Failed to place schematic at region";
    private static final String ERROR_PLACE_SCHEMATIC_DETAILS = "Region: %s, File: %s";
    private static final String LOG_HIDEOUTS_LOADED = "Tous les hideouts ont été chargés en mémoire. (Count: %d)";

    private final HideoutDatabase hideoutDatabase;
    private final Map<Integer, Hideout> hideoutCache;

    /**
     * Initialise le gestionnaire de hideout en chargeant la base de données des hideouts
     * et en initialisant le cache des hideouts.
     */
    public HideoutManager() {
        this.hideoutDatabase = Main.getInstance().getHideoutDatabase();
        this.hideoutCache = new HashMap<>();
    }

    /**
     * Charge ou crée un hideout pour un gang lorsqu'un joueur se connecte pour la première fois.
     *
     * @param gangOwnerId L'identifiant du propriétaire du gang.
     * @return Un CompletableFuture contenant un SimpleEntry avec le hideout chargé ou nouvellement créé,
     *         et un boolean indiquant s'il s'agit d'un nouveau hideout.
     */
    public CompletableFuture<SimpleEntry<Hideout, Boolean>> loadOrCreateHideoutForGang(int gangOwnerId) {
        return findUnusedHideout().thenCompose(optionalHideout -> {
            if (optionalHideout.isPresent()) {
                Hideout hideout = optionalHideout.get();
                hideout.setGangOwnerId(gangOwnerId);

                updateHideout(hideout);
                return CompletableFuture.completedFuture(new SimpleEntry<>(hideout, false));
            } else {
                return calculateNewHideout().thenCompose(hideout -> {
                    hideout.setGangOwnerId(gangOwnerId);
                    return addHideout(hideout).thenApply(h -> new SimpleEntry<>(h, true));
                });
            }
        });
    }

    /**
     * Charge tous les hideouts en mémoire lors du démarrage du plugin.
     */
    public void loadAllHideouts() {
        hideoutDatabase.getAllHideouts().thenAccept(hideouts -> {
            hideouts.forEach(hideout -> hideoutCache.put(hideout.getId(), hideout));
            Common.log(String.format(LOG_HIDEOUTS_LOADED, hideoutCache.size()));
        });
    }

    /**
     * Récupère un hideout en mémoire en fonction de l'identifiant du gang propriétaire.
     *
     * @param gangOwnerId L'identifiant du gang associé au hideout à récupérer.
     * @return Un Optional contenant le hideout s'il est trouvé.
     */
    public Optional<Hideout> getHideoutByGangOwnerId(int gangOwnerId) {
        return hideoutCache.values().stream()
                .filter(hideout -> hideout.getGangOwnerId() == gangOwnerId)
                .findFirst();
    }

    /**
     * Récupère un hideout en mémoire en fonction de son identifiant.
     *
     * @param id L'identifiant du hideout à récupérer.
     * @return Un Optional contenant le hideout s'il est trouvé.
     */
    public Optional<Hideout> getHideoutById(int id) {
        return Optional.ofNullable(hideoutCache.get(id));
    }

    /**
     * Ajoute un nouveau hideout en mémoire et le sauvegarde dans la base de données.
     *
     * @param hideout Le hideout à ajouter.
     * @return Un CompletableFuture contenant le hideout ajouté avec son identifiant généré.
     */
    public CompletableFuture<Hideout> addHideout(Hideout hideout) {
        return hideoutDatabase.addHideout(hideout).thenApply(generatedId -> {
            hideout.setId(generatedId);
            hideoutCache.put(generatedId, hideout);
            return hideout;
        });
    }

    /**
     * Met à jour un hideout existant en mémoire et dans la base de données.
     *
     * @param hideout Le hideout à mettre à jour.
     */
    public void updateHideout(Hideout hideout) {
        hideoutCache.put(hideout.getId(), hideout);
        hideoutDatabase.updateHideout(hideout);
    }

    /**
     * Récupère tous les identifiants des hideouts actuellement en mémoire.
     *
     * @return Une liste des identifiants de tous les hideouts.
     */
    public List<Integer> getAllHideoutIds() {
        return new ArrayList<>(hideoutCache.keySet());
    }

    /**
     * Place une schématique dans la région spécifiée de manière asynchrone.
     *
     * @param region La région où placer la schématique.
     * @return Un CompletableFuture indiquant la fin de l'opération.
     */
    public CompletableFuture<Void> placeSchematicAtRegion(Region region) {
        return CompletableFuture.runAsync(() -> {
            File schematicFile = Schematic.getHideout();

            ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(schematicFile);
            if (clipboardFormat == null) {
                Common.throwError(new NullPointerException(), ERROR_PLACE_SCHEMATIC, String.format(ERROR_PLACE_SCHEMATIC_DETAILS, region, schematicFile.getPath()));
                return;
            }

            try (EditSession editSession = createEditSession(region.getWorld());
                 ClipboardReader clipboardReader = clipboardFormat.getReader(new FileInputStream(schematicFile))) {

                 Clipboard clipboard = clipboardReader.read();
                 BlockVector3 clipboardCenter = clipboard.getRegion().getCenter().toBlockPoint();
                 clipboard.setOrigin(clipboardCenter);

                 Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .ignoreAirBlocks(true)
                        .to(BukkitAdapter.asBlockVector(region.getCenter()))
                        .build();

                 Operations.completeBlindly(operation);

            } catch (IOException exception) {
                Common.throwError(exception, ERROR_PLACE_SCHEMATIC, String.format(ERROR_PLACE_SCHEMATIC_DETAILS, region, schematicFile.getPath()));
            }
        });
    }

    /**
     * Calcule un nouveau Hideout en fonction de la dernière région utilisée.
     *
     * @return Un CompletableFuture contenant le nouveau Hideout.
     */

    public CompletableFuture<Hideout> calculateNewHideout() {
        return hideoutDatabase.getLastHideout().thenApply(lastHideout -> {
            HideoutBuilder builder = new HideoutBuilder();
            return builder.buildNewHideout(lastHideout.get());
        });
    }

    // Méthodes privées
    /**
     * Recherche un hideout non utilisé, c'est-à-dire un hideout dont le gangOwnerId est égal à 0.
     *
     * @return Un CompletableFuture contenant un Optional avec le hideout non utilisé, s'il est trouvé.
     */
    private CompletableFuture<Optional<Hideout>> findUnusedHideout() {
        return CompletableFuture.supplyAsync(() -> hideoutCache.values().stream()
                .filter(hideout -> hideout.getGangOwnerId() == 0)
                .findFirst());
    }

    /**
     * Crée une nouvelle session d'édition WorldEdit pour le monde spécifié.
     *
     * @param world Le monde où l'édition aura lieu.
     * @return Une nouvelle instance d'EditSession pour effectuer des opérations d'édition.
     */
    private EditSession createEditSession(World world) {
        final EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world));
        editSession.setSideEffectApplier(SideEffectSet.defaults());
        return editSession;
    }
}