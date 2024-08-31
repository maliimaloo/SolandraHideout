package com.solandra.hideout.manager.builder;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.solandra.hideout.model.Hideout;
import com.solandra.hideout.model.Mine;
import com.solandra.hideout.utils.Schematic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.region.Region;

import java.util.Optional;

public class HideoutBuilder {

    private static final String WORLD_NAME = "Hideout";
    private static final int INTERVAL_BETWEEN_HIDEOUTS = 300;
    private static final double MINE_PRIMARY_OFFSET_X = 215;
    private static final double MINE_PRIMARY_OFFSET_Y = 154;
    private static final double MINE_PRIMARY_OFFSET_Z = 176;
    private static final double MINE_SECONDARY_OFFSET_X = 165;
    private static final double MINE_SECONDARY_OFFSET_Y = 2;
    private static final double MINE_SECONDARY_OFFSET_Z = 126;
    private static final double SPAWN_OFFSET_X = 235.500;
    private static final double SPAWN_OFFSET_Y = 155;
    private static final double SPAWN_OFFSET_Z = 151.500;
    private static final float SPAWN_YAW = 91;
    private static final float SPAWN_PITCH = -5;

    private static final String ERROR_CLIPBOARD_NULL = "Impossible de calculer la nouvelle région, Clipboard is null!";

    public Hideout buildNewHideout(Hideout lastHideout) {
        Location startLocation = determineStartLocation(lastHideout);
        Clipboard clipboard = loadClipboard();

        if (clipboard == null) {
            Common.throwError(new IllegalArgumentException(ERROR_CLIPBOARD_NULL));
            return null;
        }

        BlockVector3 dimensions = clipboard.getDimensions();
        Region newMainRegion = createMainRegion(startLocation, dimensions);
        Mine newMine = createMineRegion(startLocation);
        Location spawnLocation = createSpawnLocation(startLocation);

        return new Hideout(0, newMainRegion, newMine, spawnLocation, 0);
    }

    private Location determineStartLocation(Hideout lastHideout) {
        Location startLocation = new Location(Bukkit.getWorld(WORLD_NAME), 0, 0, 0);

        if (lastHideout != null) {
            Region lastMainRegion = lastHideout.getMainRegion();
            startLocation = lastMainRegion.getPrimary().clone().add(INTERVAL_BETWEEN_HIDEOUTS + getClipboardDimensions().getX(), 0, 0);
        }

        return startLocation;
    }

    private Clipboard loadClipboard() {
        return Schematic.loadSchematic(Schematic.getHideout());
    }

    private BlockVector3 getClipboardDimensions() {
        Clipboard clipboard = loadClipboard();
        return clipboard != null ? clipboard.getDimensions() : BlockVector3.ZERO;
    }

    private Region createMainRegion(Location startLocation, BlockVector3 dimensions) {
        Location newMainRegionPrimary = startLocation.clone();
        Location newMainRegionSecondary = startLocation.clone().add(dimensions.getX(), dimensions.getY(), dimensions.getZ());
        return new Region(newMainRegionPrimary, newMainRegionSecondary);
    }

    private Mine createMineRegion(Location startLocation) {
        Location newMineRegionPrimary = startLocation.clone().add(MINE_PRIMARY_OFFSET_X, MINE_PRIMARY_OFFSET_Y, MINE_PRIMARY_OFFSET_Z);
        Location newMineRegionSecondary = startLocation.clone().add(MINE_SECONDARY_OFFSET_X, MINE_SECONDARY_OFFSET_Y, MINE_SECONDARY_OFFSET_Z);
        Region region = new Region(newMineRegionPrimary, newMineRegionSecondary);

        return new Mine(region, 0);
    }

    private Location createSpawnLocation(Location startLocation) {
        Location spawnLocation = startLocation.clone().add(SPAWN_OFFSET_X, SPAWN_OFFSET_Y, SPAWN_OFFSET_Z);
        spawnLocation.setYaw(SPAWN_YAW);
        spawnLocation.setPitch(SPAWN_PITCH);
        return spawnLocation;
    }
}