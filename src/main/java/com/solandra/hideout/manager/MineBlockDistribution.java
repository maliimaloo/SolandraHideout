package com.solandra.hideout.manager;

import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MineBlockDistribution {

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
     * @return Une map associant chaque Material à son pourcentage d'apparition.
     */
    public Map<BlockType, Double> getBlockDistributionForLevel(int level) {
        Map<BlockType, Double> blockDistribution = new HashMap<>();
        int cycle = (level - 1) / 5;  // Détermine à quel "cycle" de niveaux, nous sommes
        int cycleLevel = (level - 1) % 5 + 1;  // Détermine le niveau à l'intérieur du cycle

        for (int i = 0; i < blockType.size(); i++) {
            double percentage;
            if (i < cycle) {
                percentage = 0;  // Les matériaux des cycles précédents sont à 0%
            } else if (i == cycle) {
                percentage = Math.max(0, 70 - 15 * (cycleLevel - 1));  // Le pourcentage du matériau en cours de réduction
            } else if (i == cycle + 1) {
                percentage = Math.min(70, 20 + 10 * (cycleLevel - 1));  // Le matériau qui augmente en pourcentage
            } else if (i == cycle + 2) {
                percentage = Math.min(70, 10 + 10 * (cycleLevel - 1));  // Le matériau qui commence à augmenter
            } else {
                percentage = 0;  // Matériaux futurs avec un pourcentage à 0%
            }
            blockDistribution.put(blockType.get(i), percentage);
        }

        return blockDistribution;
    }
}