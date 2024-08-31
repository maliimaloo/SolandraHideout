package com.solandra.hideout.manager;

import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MineBlockDistribution {
    // Constantes pour éviter les magic numbers
    private static final double INITIAL_PERCENTAGE = 70.0;
    private static final double DECREASE_STEP = 15.0;
    private static final double MINIMUM_PERCENTAGE = 0.0;
    private static final double INCREASE_STEP_1 = 10.0;
    private static final double INCREASE_STEP_2 = 20.0;
    private static final int LEVELS_PER_CYCLE = 5;

    private final List<BlockType> blockType;

    public MineBlockDistribution() {
        this.blockType = List.of(
                BlockTypes.STONE,
                BlockTypes.COAL_ORE,
                BlockTypes.IRON_ORE,
                BlockTypes.DIAMOND_ORE,
                BlockTypes.EMERALD_ORE,
                BlockTypes.GOLD_ORE,
                BlockTypes.REDSTONE_ORE,
                BlockTypes.LAPIS_ORE,
                BlockTypes.QUARTZ_BLOCK,
                BlockTypes.ANCIENT_DEBRIS
        );
    }


    /**
     * Génère la distribution des blocs en fonction du niveau de la mine.
     *
     * @param level Le niveau actuel de la mine.
     * @return Une map associant chaque BlockType à son pourcentage d'apparition.
     */
    public Map<BlockType, Double> getBlockDistributionForLevel(int level) {
        Map<BlockType, Double> blockDistribution = new HashMap<>();
        int cycle = (level - 1) / LEVELS_PER_CYCLE;
        int cycleLevel = (level - 1) % LEVELS_PER_CYCLE + 1;

        for (int i = 0; i < blockType.size(); i++) {
            double percentage;

            if (i < cycle) {
                percentage = MINIMUM_PERCENTAGE;
            } else if (i == cycle) {
                percentage = Math.max(MINIMUM_PERCENTAGE, INITIAL_PERCENTAGE - DECREASE_STEP * (cycleLevel - 1));
            } else if (i == cycle + 1) {
                percentage = Math.min(INITIAL_PERCENTAGE, INCREASE_STEP_2 + INCREASE_STEP_1 * (cycleLevel - 1));
            } else if (i == cycle + 2) {
                percentage = Math.min(INITIAL_PERCENTAGE, INCREASE_STEP_1 + INCREASE_STEP_1 * (cycleLevel - 1));
            } else {
                percentage = MINIMUM_PERCENTAGE;
            }

            blockDistribution.put(blockType.get(i), percentage);
        }

        return blockDistribution;
    }
}