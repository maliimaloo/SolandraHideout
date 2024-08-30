package com.solandra.hideout.model;

import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.model.ConfigSerializable;
import org.mineacademy.fo.region.Region;

public class Mine implements ConfigSerializable {

    private final Region region;
    private int level;

    /**
     * Constructeur pour la classe Mine.
     *
     * @param region La région associée à cette mine.
     * @param level  Le niveau initial de la mine.
     */
    public Mine(Region region, int level) {
        this.region = region;
        this.level = level;
    }

    /**
     * Retourne la région associée à cette mine.
     *
     * @return La région de la mine.
     */
    public Region getRegion() {
        return region;
    }

    /**
     * Retourne le niveau actuel de la mine.
     *
     * @return Le niveau actuel de la mine.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Définit un nouveau niveau pour la mine.
     *
     * @param level Le nouveau niveau à définir.
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Incrémente le niveau de la mine d'une unité.
     */
    public void addLevel() {
        this.level += 1;
    }

    /**
     * Met à jour la région de la mine avec de nouvelles coordonnées.
     *
     * @param region La nouvelle région à définir pour la mine.
     */
    public void setMineRegion(Region region) {
        this.region.setPrimary(region.getPrimary());
        this.region.setSecondary(region.getSecondary());
    }

    /**
     * Sérialise cette instance de Mine en une {@link SerializedMap}.
     *
     * @return Une map contenant les données sérialisées de la mine.
     */
    @Override
    public SerializedMap serialize() {
        return SerializedMap.ofArray(
                "MineRegion", this.region.serialize(),
                "Level", this.level
        );
    }

    /**
     * Désérialise une map en une instance de Mine.
     *
     * @param map La map contenant les données à désérialiser.
     * @return Une nouvelle instance de Mine.
     */
    public static Mine deserialize(SerializedMap map) {
        Region mineRegion = Region.deserialize(map.getMap("MineRegion"));
        int level = map.getInteger("Level");

        return new Mine(mineRegion, level);
    }
}