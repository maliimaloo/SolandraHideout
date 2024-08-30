package com.solandra.hideout.model;

import com.solandra.hideout.Main;
import com.solandra.prisoncore.api.PlayerAPI;
import com.solandra.prisoncore.model.PrisonPlayer;
import net.brcdev.gangs.GangsPlugin;
import net.brcdev.gangs.gang.Gang;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.region.Region;

import java.util.Optional;

/**
 * Représente un hideout associé à un gang dans le jeu.
 * Un hideout comprend une région principale, une mine, un point d'apparition et un propriétaire (gang).
 */
public class Hideout {
    private int id;
    private final Region mainRegion;
    private final Mine mine;
    private final Location spawnLocation;
    private int gangOwnerId;

    /**
     * Constructeur complet pour la classe Hideout.
     *
     * @param id           L'identifiant unique du hideout.
     * @param mainRegion   La région principale associée à ce hideout.
     * @param mine         La mine associée à ce hideout.
     * @param spawnLocation La localisation du point d'apparition pour ce hideout.
     * @param gangOwnerId  L'identifiant du gang propriétaire de ce hideout.
     */
    public Hideout(int id, Region mainRegion, Mine mine, Location spawnLocation, int gangOwnerId) {
        this.id = id;
        this.mainRegion = mainRegion;
        this.mine = mine;
        this.spawnLocation = spawnLocation;
        this.gangOwnerId = gangOwnerId;
    }

    /**
     * Retourne l'identifiant unique de ce hideout.
     *
     * @return L'identifiant du hideout.
     */
    public int getId() {
        return id;
    }

    /**
     * Définit l'identifiant unique de ce hideout.
     *
     * @param id Le nouvel identifiant du hideout.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retourne la région principale associée à ce hideout.
     *
     * @return La région principale du hideout.
     */
    public Region getMainRegion() {
        return mainRegion;
    }

    /**
     * Retourne la mine associée à ce hideout.
     *
     * @return La mine du hideout.
     */
    public Mine getMine() {
        return mine;
    }

    /**
     * Retourne la localisation du point d'apparition pour ce hideout.
     *
     * @return Le point d'apparition du hideout.
     */
    public Location getSpawnLocation() {
        return spawnLocation;
    }

    /**
     * Retourne l'identifiant du gang propriétaire de ce hideout.
     *
     * @return L'identifiant du gang propriétaire.
     */
    public int getGangOwnerId() {
        return gangOwnerId;
    }

    /**
     * Définit l'identifiant du gang propriétaire de ce hideout.
     *
     * @param gangOwnerId Le nouvel identifiant du gang propriétaire.
     */
    public void setGangOwnerId(int gangOwnerId) {
        this.gangOwnerId = gangOwnerId;
    }

    /**
     * Calcule le prestige total du gang en parcourant les membres du gang et en additionnant leur prestige.
     * Cette méthode fonctionne de manière asynchrone pour les membres hors-ligne.
     *
     * @return Un CompletableFuture contenant le prestige total du gang.
     */
    public Integer getPrestige() {
        Gang gang = GangsPlugin.getInstance().getGangManager().getGang(this.gangOwnerId);
        if (gang == null) {
            Common.throwError(new NullPointerException(), "Gang is null");
            return 0;
        }

        PlayerAPI playerAPI = Main.getInstance().getPlayerAPI();
        if (playerAPI == null) {
            Common.throwError(new NullPointerException(), "PlayerAPI is null");
            return 0;
        }

        int prestigeTotal = 0;
        for (OfflinePlayer member : gang.getAllMembers()) {
            Optional<PrisonPlayer> prisonPlayer = playerAPI.getPlayer(member.getUniqueId());
            if (prisonPlayer.isPresent()) {
                prestigeTotal += prisonPlayer.get().getPrestige();
            }
        }

        return prestigeTotal;
    }
}