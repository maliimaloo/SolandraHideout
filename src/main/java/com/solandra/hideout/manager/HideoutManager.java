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
import com.solandra.hideout.model.Hideout;
import com.solandra.hideout.model.Mine;
import com.solandra.hideout.utils.Schematic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

    // Constantes
    private static final String WORLD_NAME = "Hideout";
    private static final String ERROR_PLACE_SCHEMATIC = "Failed to place schematic at region";
    private static final String ERROR_PLACE_SCHEMATIC_DETAILS = "Region: %s, File: %s";
    private static final String ERROR_CLIPBOARD_NULL = "Impossible de calculer la nouvelle région, Clipboard is null!";
    private static final String LOG_HIDEOUTS_LOADED = "Tous les hideouts ont été chargés en mémoire. (Count: %d)";
    private static final int INTERVAL_BETWEEN_HIDEOUTS = 300;

    private final HideoutDatabase hideoutDatabase;
    private final Map<Integer, Hideout> hideoutCache;

    /**
     * Initialise le gestionnaire de hideout en chargeant la base de données des hideouts
     * et en initialisant le cache des hideouts.
     *
     * @param plugin L'instance principale du plugin.
     */
    public HideoutManager(Main plugin) {
        this.hideoutDatabase = plugin.getHideoutDatabase();
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

            try (EditSession editSession = createEditSession(region.getWorld())) {
                ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(schematicFile);

                try {
                    assert clipboardFormat != null;
                    try (ClipboardReader clipboardReader = clipboardFormat.getReader(new FileInputStream(schematicFile))) {
                        Clipboard clipboard = clipboardReader.read();

                        BlockVector3 clipboardCenter = clipboard.getRegion().getCenter().toBlockPoint();
                        clipboard.setOrigin(clipboardCenter);

                        Operation operation = new ClipboardHolder(clipboard)
                                .createPaste(editSession)
                                .ignoreAirBlocks(true)
                                .to(BukkitAdapter.asBlockVector(region.getCenter()))
                                .build();

                        Operations.completeBlindly(operation);
                    }
                } catch (NullPointerException exception) {
                    Common.throwError(exception, ERROR_PLACE_SCHEMATIC, String.format(ERROR_PLACE_SCHEMATIC_DETAILS, region, schematicFile.getPath()));
                }
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
            Location startLocation = new Location(Bukkit.getWorld(WORLD_NAME), 0, 0, 0);

            Clipboard clipboard = Schematic.loadSchematic(Schematic.getHideout());
            if (clipboard == null) {
                Common.throwError(new IllegalArgumentException(ERROR_CLIPBOARD_NULL));
                return null;
            }

            if (lastHideout.isPresent()) {
                Region lastMainRegion = lastHideout.get().getMainRegion();
                startLocation = lastMainRegion.getPrimary().clone().add(INTERVAL_BETWEEN_HIDEOUTS + clipboard.getDimensions().x(), 0, 0);
            }

            BlockVector3 dimensions = clipboard.getDimensions();

            Location newMainRegionPrimary = startLocation.clone();
            Location newMainRegionSecondary = startLocation.clone().add(dimensions.x(), dimensions.y(), dimensions.z());
            Region newMainRegion = new Region(newMainRegionPrimary, newMainRegionSecondary);

            Location newMineRegionPrimary = startLocation.clone().add(215, 154, 176);
            Location newMineRegionSecondary = startLocation.clone().add(165, 2, 126);
            Region newMineRegion = new Region(newMineRegionPrimary, newMineRegionSecondary);
            Mine newMine = new Mine(newMineRegion, 0);

            Location spawnLocation = startLocation.clone().add(235.500, 155, 151.500);
            spawnLocation.setYaw(91);
            spawnLocation.setPitch(-5);

            return new Hideout(0, newMainRegion, newMine, spawnLocation, 0);
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