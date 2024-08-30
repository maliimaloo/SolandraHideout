package com.solandra.hideout.api;

import com.solandra.hideout.model.Mine;
import com.solandra.hideout.model.Hideout;
import org.bukkit.entity.Player;
import org.mineacademy.fo.region.Region;

import java.util.concurrent.CompletableFuture;

/**
 * Interface API pour la gestion des mines.
 * Fournit des méthodes pour améliorer, réinitialiser et remplir les mines avec des blocs.
 */
public interface MineAPI {

    /**
     * Améliore la mine du hideout si le joueur a suffisamment de fonds et si le cooldown est terminé.
     *
     * @param executor Le joueur qui initie l'amélioration.
     * @param hideout  Le hideout contenant la mine.
     */
    void upgradeMine(Player executor, Hideout hideout);

    /**
     * Réinitialise la mine et remet son niveau à zéro.
     *
     * @param hideout Le hideout contenant la mine.
     */
    void resetMine(Hideout hideout);

    /**
     * Remplit la région de la mine avec des blocs en fonction du niveau de la mine.
     *
     * @param hideout Le hideout contenant la mine.
     * @return Un CompletableFuture indiquant la fin de l'opération de remplissage.
     */
    CompletableFuture<Void> fillWithBlocks(Hideout hideout);

    /**
     * Retourne la région associée à cette mine.
     *
     * @param hideout Le hideout contenant la mine
     * @return La région de la mine.
     */
    Region getMineRegion(Hideout hideout);

    /**
     * Retourne le niveau actuel de la mine.
     *
     * @param hideout Le hideout contenant la mine
     * @return Le niveau actuel de la mine.
     */
    int getMineLevel(Hideout hideout);

    /**
     * Définit un nouveau niveau pour la mine.
     *
     * @param hideout Le hideout contenant la mine
     * @param level Le nouveau niveau à définir.
     */
    void setMineLevel(Hideout hideout, int level);

    /**
     * Incrémente le niveau de la mine d'une unité.
     *
     * @param hideout Le hideout contenant la mine
     */
    void addMineLevel(Hideout hideout);

    /**
     * Met à jour la région de la mine avec de nouvelles coordonnées.
     *
     * @param hideout Le hideout contenant la mine
     * @param region La nouvelle région à définir pour la mine.
     */
    void setMineRegion(Hideout hideout, Region region);
}