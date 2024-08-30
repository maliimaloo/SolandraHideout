package com.solandra.hideout.manager;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.solandra.hideout.Main;
import com.solandra.hideout.model.Hideout;
import com.solandra.hideout.model.Mine;
import net.brcdev.gangs.GangsPlugin;
import net.brcdev.gangs.gang.Gang;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.region.Region;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Gère les opérations liées aux mines, telles que l'amélioration et le remplissage avec des blocs.
 */
public class MineManager {
    private final HideoutManager hideoutManager;

    private static final long UPGRADE_COOLDOWN = 5000; // 5 secondes en millisecondes
    private static final int MAX_LEVEL = 15; // Niveau maximum pour les mines

    // Messages constants
    private static final String MSG_MAX_LEVEL_REACHED = "Vous avez atteint le niveau maximum de l'upgrade.";
    private static final String MSG_UPGRADE_COOLDOWN = "Veuillez attendre encore %d secondes avant de pouvoir améliorer la mine à nouveau.";
    private static final String MSG_UPGRADE_SUCCESS = "Vous venez d'améliorer votre mine au niveau %d.";
    private static final String MSG_GANG_NOTIFY = "La mine vient d'être améliorée au niveau %d par %s.";

    private final ConcurrentMap<Integer, Long> lastUpgradeTimestamps;

    public MineManager(Main plugin) {
        this.hideoutManager = plugin.getHideoutManager();
        this.lastUpgradeTimestamps = new ConcurrentHashMap<>();
    }

    /**
     * Améliore la mine du hideout si le joueur a suffisamment de fonds et si le cooldown est terminé.
     *
     * @param executor Le joueur qui initie l'amélioration.
     * @param hideout  Le hideout contenant la mine.
     */
    public void upgradeMine(Player executor, Hideout hideout) {
        if (isMaxLevelReached(hideout.getMine())) {
            Messenger.error(executor, MSG_MAX_LEVEL_REACHED);
            return;
        }

        if (isUpgradeOnCooldown(executor, hideout.getId())) {
            return;
        }

        executeMineUpgrade(executor, hideout);
    }

    /**
     * Réinitialise la mine et remet son niveau à zéro.
     *
     * @param hideout Le hideout contenant la mine.
     */
    public void resetMine(Hideout hideout) {
        Common.log("Current Min Region: Primary |x: " + hideout.getMine().getRegion().getPrimary().getX() + ", z: " + hideout.getMine().getRegion().getPrimary().getZ() + "| Secondary |x: " + hideout.getMine().getRegion().getSecondary().getX() + ", z: " + hideout.getMine().getRegion().getSecondary().getZ());
        Region minRegion = calculateMinRegionBasedOnLevel(hideout.getMine());
        hideout.getMine().setMineRegion(minRegion);
        hideout.getMine().setLevel(0);

        updateHideout(hideout);
        Common.log("Next Min Region: Primary |x: " + minRegion.getPrimary().getX() + ", z: " + minRegion.getPrimary().getZ() + "| Secondary |x: " + minRegion.getSecondary().getX() + ", z: " + minRegion.getSecondary().getZ());
        Common.log("Current Min Region: Primary |x: " + hideout.getMine().getRegion().getPrimary().getX() + ", z: " + hideout.getMine().getRegion().getPrimary().getZ() + "| Secondary |x: " + hideout.getMine().getRegion().getSecondary().getX() + ", z: " + hideout.getMine().getRegion().getSecondary().getZ());
    }

    /**
     * Remplit la région de la mine avec des blocs en fonction du niveau de la mine.
     *
     * @param hideout Le hideout contenant la mine.
     */
    public CompletableFuture<Void> fillWithBlocks(Hideout hideout) {
        return CompletableFuture.runAsync(() -> {
            Map<BlockType, Double> blockDistribution = getBlockDistributionForMine(hideout);
            if (blockDistribution == null || blockDistribution.isEmpty()) {
                return;
            }

            List<BlockState> blocksToPlace = prepareBlocksToPlace(blockDistribution, hideout.getMine().getRegion().getBlocks().size());
            placeBlocksInMine(hideout, blocksToPlace);
        });
    }

    // Méthodes privées

    /**
     * Exécute l'amélioration de la mine de manière asynchrone.
     *
     * @param executor Le joueur qui initie l'amélioration.
     * @param hideout  Le hideout contenant la mine.
     */
    private void executeMineUpgrade(Player executor, Hideout hideout) {
        lastUpgradeTimestamps.put(hideout.getId(), System.currentTimeMillis());

        CompletableFuture.runAsync(() -> {
            upgradeMineLevel(hideout);
            updateMineRegion(hideout);
            updateHideout(hideout);

            teleportPlayersOutsideMine(hideout);

            fillWithBlocks(hideout).thenRun(() -> {
                notifyGangMembers(hideout, executor);
                Messenger.success(executor, String.format(MSG_UPGRADE_SUCCESS, hideout.getMine().getLevel()));
            });
        });
    }

    /**
     * Vérifie si la mine a atteint le niveau maximum.
     *
     * @param mine La mine à vérifier.
     * @return True si le niveau maximum est atteint, sinon False.
     */
    private boolean isMaxLevelReached(Mine mine) {
        return mine.getLevel() >= MAX_LEVEL;
    }

    /**
     * Vérifie si un cooldown est en cours pour l'amélioration de la mine.
     *
     * @param executor  Le joueur qui initie l'amélioration.
     * @param hideoutId L'ID du hideout.
     * @return True si un cooldown est en cours, sinon False.
     */
    private boolean isUpgradeOnCooldown(Player executor, Integer hideoutId) {
        long currentTime = System.currentTimeMillis();
        Long lastUpgradeTime = lastUpgradeTimestamps.get(hideoutId);

        if (lastUpgradeTime != null && (currentTime - lastUpgradeTime) < UPGRADE_COOLDOWN) {
            long remainingTime = (UPGRADE_COOLDOWN - (currentTime - lastUpgradeTime)) / 1000;
            Messenger.error(executor, String.format(MSG_UPGRADE_COOLDOWN, remainingTime));
            return true;
        }

        return false;
    }

    /**
     * Améliore le niveau de la mine.
     *
     * @param hideout Le hideout contenant la mine.
     */
    private void upgradeMineLevel(Hideout hideout) {
        hideout.getMine().addLevel();
    }

    /**
     * Met à jour la région de la mine après amélioration.
     *
     * @param hideout Le hideout contenant la mine.
     */
    private void updateMineRegion(Hideout hideout) {
        hideout.getMine().setMineRegion(calculateNextRegion(hideout));
    }

    /**
     * Met à jour l'hideout après amélioration.
     *
     * @param hideout Le hideout contenant la mine.
     */
    private void updateHideout(Hideout hideout) {
        hideoutManager.updateHideout(hideout);
    }

    /**
     * Téléporte les joueurs hors de la mine après une amélioration.
     *
     * @param hideout Le hideout contenant la mine.
     */
    private void teleportPlayersOutsideMine(Hideout hideout) {
        for (Entity entity : hideout.getMine().getRegion().getEntities()) {
            if (entity instanceof Player) {
                Common.runLater(() -> Objects.requireNonNull(((Player) entity).getPlayer()).teleport(hideout.getSpawnLocation()));
            }
        }
    }

    /**
     * Notifie les membres du gang de l'amélioration de la mine.
     *
     * @param hideout  Le hideout contenant la mine.
     * @param executor Le joueur qui a initié l'amélioration.
     */
    private void notifyGangMembers(Hideout hideout, Player executor) {
        Gang gang = GangsPlugin.getInstance().getGangManager().getGang(hideout.getGangOwnerId());
        gang.getAllMembers().forEach(offlinePlayer -> {
            if (offlinePlayer.isOnline() && !Objects.requireNonNull(offlinePlayer.getName()).equalsIgnoreCase(executor.getName())) {
                Messenger.success(offlinePlayer.getPlayer(), String.format(MSG_GANG_NOTIFY, hideout.getMine().getLevel(), executor.getName()));
            }
        });
    }

    /**
     * Prépare les blocs à placer dans la mine en fonction de la distribution.
     *
     * @param blockDistribution La distribution des blocs en fonction du niveau.
     * @param totalBlocks       Le nombre total de blocs dans la région.
     * @return Une liste de blocs à placer.
     */
    private List<BlockState> prepareBlocksToPlace(Map<BlockType, Double> blockDistribution, int totalBlocks) {
        List<BlockState> blocksToPlace = new ArrayList<>();

        blockDistribution.forEach((blockType, percentage) -> {
            int blockCount = (int) Math.round(percentage / 100.0 * totalBlocks);
            BlockState block = blockType.getDefaultState();

            for (int i = 0; i < blockCount; i++) {
                blocksToPlace.add(block);
            }
        });

        Collections.shuffle(blocksToPlace, new Random());
        return blocksToPlace;
    }

    /**
     * Place les blocs dans la mine en fonction de la liste fournie.
     *
     * @param hideout      Le hideout contenant la mine.
     * @param blocksToPlace La liste des blocs à placer.
     */
    private void placeBlocksInMine(Hideout hideout, List<BlockState> blocksToPlace) {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(hideout.getMine().getRegion().getWorld()))) {
            List<BlockVector3> positions = getBlocksAsVectors(hideout.getMine().getRegion());

            if (positions.size() > blocksToPlace.size()) {
                BlockState defaultBlock = BukkitAdapter.adapt(Material.STONE.createBlockData());
                while (blocksToPlace.size() < positions.size()) {
                    blocksToPlace.add(defaultBlock);
                }
            } else if (positions.size() < blocksToPlace.size()) {
                blocksToPlace = blocksToPlace.subList(0, positions.size());
            }

            for (int i = 0; i < positions.size(); i++) {
                try {
                    editSession.setBlock(positions.get(i), blocksToPlace.get(i));
                } catch (Exception e) {
                    Common.throwError(e, "Erreur lors du placement du bloc à la position: " + positions.get(i) + ". Bloc: " + blocksToPlace.get(i).getAsString());
                }
            }

            Operations.completeBlindly(editSession.commit());
        }
    }

    /**
     * Récupère la distribution des blocs pour un niveau spécifique de la mine.
     *
     * @param hideout l'hideout pour laquelle récupérer la distribution.
     * @return La distribution des blocs sous forme de Map.
     */
    private Map<BlockType, Double> getBlockDistributionForMine(Hideout hideout) {
        MineBlockDistribution mineBlockDistribution = new MineBlockDistribution();
        return mineBlockDistribution.getBlockDistributionForLevel(hideout.getPrestige());
    }

    /**
     * Calcule la région agrandie pour le prochain niveau de la mine.
     *
     * @param hideout Le hideout de la mine.
     * @return La nouvelle région après amélioration.
     */
    private Region calculateNextRegion(Hideout hideout) {
        Location primary = hideout.getMine().getRegion().getPrimary().clone().add(0, 0, 5);
        Location secondary = hideout.getMine().getRegion().getSecondary().clone().subtract(10, 0, 5);
        return new Region(primary, secondary);
    }

    /**
     * Calcule la région minimale basée sur le niveau 0 de la mine.
     *
     * @param mine La mine dont la région minimale doit être calculée.
     * @return La région calculée pour le niveau 0.
     */
    private Region calculateMinRegionBasedOnLevel(Mine mine) {
        Location primaryMin = mine.getRegion().getPrimary().clone().subtract(0, 0, 5 * mine.getLevel());
        Location secondaryMin = mine.getRegion().getSecondary().clone().add(10 * mine.getLevel(), 0, 5 * mine.getLevel());
        return new Region(primaryMin, secondaryMin);
    }

    /**
     * Retourne une liste des positions des blocs dans la région sous forme de BlockVector3.
     *
     * @param region La région de la mine.
     * @return Une liste de BlockVector3 représentant les positions des blocs dans la région.
     */
    private List<BlockVector3> getBlocksAsVectors(Region region) {
        List<BlockVector3> blockVectors = new ArrayList<>();

        for (Block block : region.getBlocks()) {
            blockVectors.add(BlockVector3.at(block.getX(), block.getY(), block.getZ()));
        }

        return blockVectors;
    }
}