package com.solandra.hideout.listeners;

import com.solandra.hideout.Main;
import com.solandra.hideout.manager.HideoutManager;
import com.solandra.hideout.manager.MineManager;
import com.solandra.hideout.model.Hideout;
import net.brcdev.gangs.event.PlayerJoinGangEvent;
import net.brcdev.gangs.gang.Gang;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mineacademy.fo.Common;

import java.util.Optional;

public class HideoutListener implements Listener {

    // Gestionnaire des hideouts
    private final HideoutManager hideoutManager;
    private final MineManager mineManager;

    // Constantes pour les messages
    private static final String GANG_JOIN_MESSAGE = "Vous venez de rejoindre un gang !";
    private static final String HIDEOUT_READY_MESSAGE = "&aVotre hideout est prêt ! Vous avez été téléporté au centre.";
    private static final String ERROR_MESSAGE = "Impossible de placer la schématique et de téléporter le joueur.";
    private static final String ERROR_MESSAGE_TEMPLATE = "ID du gang : %d, Joueur : %s";

    /**
     * Constructeur de HideoutListener qui injecte les dépendances nécessaires.
     */
    public HideoutListener(Main plugin) {
        this.hideoutManager = plugin.getHideoutManager();
        this.mineManager = plugin.getMineManager();
    }

    /**
     * Gestion de l'événement lorsqu'un joueur rejoint un gang.
     * Si le joueur est le propriétaire du gang, un hideout est créé ou chargé,
     * puis une schématique y est placée et le joueur est téléporté.
     *
     * @param event L'événement déclenché lorsqu'un joueur rejoint un gang.
     */
    @EventHandler
    public void onGangCreate(PlayerJoinGangEvent event) {
        Gang gang = event.getGang();
        Player playerJoin = event.getPlayer();

        if (isGangOwner(gang, playerJoin)) {
            handleGangOwnerJoin(gang, playerJoin);
        } else {
            handleGangMemberJoin(playerJoin, gang.getId());
        }
    }

    /**
     * Vérifie si le joueur est le propriétaire du gang.
     *
     * @param gang       Le gang auquel le joueur a adhéré.
     * @param playerJoin Le joueur qui a rejoint le gang.
     * @return true si le joueur est le propriétaire du gang, false sinon.
     */
    private boolean isGangOwner(Gang gang, Player playerJoin) {
        return gang.getOwner() == playerJoin;
    }

    /**
     * Gère les actions nécessaires lorsque le propriétaire d'un gang rejoint.
     * Cela inclut la création ou le chargement d'un hideout, le placement d'une
     * schématique, et la téléportation du joueur au centre du hideout.
     *
     * @param gang       Le gang auquel le joueur a adhéré.
     * @param playerJoin Le joueur qui a rejoint le gang et qui est le propriétaire.
     */
    private void handleGangOwnerJoin(Gang gang, Player playerJoin) {
        hideoutManager.loadOrCreateHideoutForGang(gang.getId()).thenAccept(entry -> {
            Hideout hideout = entry.getKey();
            if (entry.getValue()) {
                placeSchematicAndTeleport(hideout, playerJoin, gang.getId());
            } else {
                teleportPlayerToHideout(playerJoin, hideout);
            }

            mineManager.fillWithBlocks(hideout);
        });
    }

    /**
     * Gère les actions nécessaires lorsque n'importe quel membre d'un gang rejoint.
     * Cela inclut la vérification de l'existence d'un hideout et la téléportation du joueur.
     *
     * @param playerJoin Le joueur qui a rejoint le gang.
     * @param gangId     L'identifiant du gang.
     */
    private void handleGangMemberJoin(Player playerJoin, int gangId) {
        Common.tell(playerJoin, GANG_JOIN_MESSAGE);
        Optional<Hideout> hideout = hideoutManager.getHideoutById(gangId);
        hideout.ifPresent(value -> teleportPlayerToHideout(playerJoin, value));
    }

    /**
     * Place la schématique dans la région spécifiée et téléporte le joueur
     * au centre du hideout une fois l'opération terminée.
     *
     * @param hideout    Le hideout dans lequel placer la schématique.
     * @param playerJoin Le joueur à téléporter une fois la schématique placée.
     * @param gangId     L'identifiant du gang pour les messages d'erreur.
     */
    private void placeSchematicAndTeleport(Hideout hideout, Player playerJoin, int gangId) {
        hideoutManager.placeSchematicAtRegion(hideout.getMainRegion())
                .thenRun(() -> teleportPlayerToHideout(playerJoin, hideout))
                .exceptionally(throwable -> handleSchematicPlacementError(throwable, gangId, playerJoin.getName()));
    }

    /**
     * Téléporte le joueur au centre de la région spécifiée.
     *
     * @param playerJoin Le joueur à téléporter.
     * @param hideout    Le hideout où téléporter le joueur.
     */
    private void teleportPlayerToHideout(Player playerJoin, Hideout hideout) {
        Common.runLater(() -> {
            playerJoin.teleport(hideout.getSpawnLocation());
            Common.tell(playerJoin, HIDEOUT_READY_MESSAGE);
        });
    }

    /**
     * Gère les erreurs qui surviennent lors du placement de la schématique ou de la téléportation.
     *
     * @param throwable  L'exception lancée pendant l'opération.
     * @param gangId     L'identifiant du gang concerné.
     * @param playerName Le nom du joueur concerné.
     * @return null pour compléter la chaîne d'appel exceptionnellement.
     */
    private Void handleSchematicPlacementError(Throwable throwable, int gangId, String playerName) {
        Common.throwError(throwable, ERROR_MESSAGE, String.format(ERROR_MESSAGE_TEMPLATE, gangId, playerName));
        return null;
    }
}