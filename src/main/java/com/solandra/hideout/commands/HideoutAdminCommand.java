package com.solandra.hideout.commands;

import com.solandra.hideout.Main;
import com.solandra.hideout.manager.HideoutManager;
import com.solandra.hideout.menu.HideoutMenu;
import com.solandra.hideout.utils.MojangAPI;
import net.brcdev.gangs.GangsPlugin;
import net.brcdev.gangs.gang.Gang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.command.SimpleCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Commande admin pour la gestion des hideouts.
 */
public class HideoutAdminCommand extends SimpleCommand {

    // Constantes pour les messages et sous-commandes
    private static final String NO_SUBCOMMAND_MESSAGE = "Veuillez spécifier une sous-commande : list, teleport <id>.";
    private static final String UNKNOWN_SUBCOMMAND_MESSAGE = "Sous-commande inconnue. Utilisez : list, teleport <id>.";
    private static final String NO_HIDEOUT_ID_MESSAGE = "Veuillez spécifier l'ID du hideout. Utilisation: /hideout teleport <id>";
    private static final String INVALID_HIDEOUT_ID_MESSAGE = "L'ID du hideout doit être un nombre entier.";
    private static final String HIDEOUT_NOT_FOUND_MESSAGE = "&cAucun hideout trouvé pour l'ID: ";
    private static final String NO_PLAYER_OR_ID_MESSAGE = "Veuillez spécifier un joueur ou un ID de hideout. Utilisation: /hoa show <player|id>";
    private static final String PLAYER_NOT_FOUND_MESSAGE = "Le joueur %s n'existe pas.";
    private static final String PLAYER_NOT_IN_GANG_MESSAGE = "Le joueur %s n'appartient à aucun gang.";
    private static final String NO_HIDEOUT_FOUND_FOR_PLAYER_MESSAGE = "&cAucun hideout trouvé pour le joueur: ";
    private static final String HIDEOUT_ADMIN_PERMISSION = "solandra.hideout.admin";

    // Fields
    private final Main plugin = Main.getInstance();
    private final HideoutManager hideoutManager = Main.getInstance().getHideoutManager();

    /**
     * Constructeur de la commande HideoutAdminCommand.
     */
    public HideoutAdminCommand(Main plugin) {
        super("hideoutadmin|hoa");

        setPermission(HIDEOUT_ADMIN_PERMISSION);
        setDescription("Commande admin des hideouts.");
    }

    /**
     * Méthode exécutée lorsque la commande est lancée.
     */
    @Override
    protected void onCommand() {
        checkConsole();
        Player executor = this.getPlayer();

        if (args.length == 0) {
            this.tellError(NO_SUBCOMMAND_MESSAGE);
            return;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "list":
                handleListCommand();
                break;
            case "show":
                handleShowCommand(executor);
                break;
            case "teleport":
                handleTeleportCommand(executor);
                break;
            default:
                this.tellError(UNKNOWN_SUBCOMMAND_MESSAGE);
                break;
        }
    }

    /**
     * Fournit les complétions de tabulation pour la commande.
     *
     * @return Une liste des complétions possibles.
     */
    @Override
    protected List<String> tabComplete() {
        if (args.length == 1) {
            return completeLastWord("list", "teleport", "show");
        }

        if (args.length == 2) {
            String subcommand = args[0].toLowerCase();

            if (subcommand.equals("teleport")) {
                return completeLastWord(hideoutManager.getAllHideoutIds());
            }

            if (subcommand.equals("show")) {
                List<String> suggestions = new ArrayList<>();
                suggestions.addAll(hideoutManager.getAllHideoutIds().stream()
                        .map(String::valueOf)
                        .toList());

                suggestions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .toList());

                return completeLastWord(suggestions);
            }
        }

        return NO_COMPLETE;
    }

    /**
     * Gère l'exécution de la sous-commande "list".
     */
    private void handleListCommand() {
        Common.runAsync(() -> {
            List<Integer> hideoutIds = hideoutManager.getAllHideoutIds();
            if (hideoutIds == null || hideoutIds.isEmpty()) {
                this.tellError("Aucun hideout trouvé.");
            } else {
                this.tell("&6--- Liste des Hideouts ---");
                hideoutIds.forEach(id -> this.tell("&eID: &f" + id));
            }
        });
    }

    /**
     * Gère l'exécution de la sous-commande "show".
     *
     * @param executor Le joueur qui exécute la commande.
     */
    private void handleShowCommand(Player executor) {
        if (args.length < 2) {
            this.tellError(NO_PLAYER_OR_ID_MESSAGE);
            return;
        }

        String target = args[1];
        if (Valid.isNumber(target)) {
            showHideoutById(executor, Integer.parseInt(target));
        } else {
            showHideoutByPlayer(executor, target);
        }
    }

    /**
     * Gère l'exécution de la sous-commande "teleport".
     *
     * @param executor Le joueur qui exécute la commande.
     */
    private void handleTeleportCommand(Player executor) {
        if (args.length < 2) {
            this.tellError(NO_HIDEOUT_ID_MESSAGE);
            return;
        }

        if (!Valid.isNumber(args[1])) {
            this.tellError(INVALID_HIDEOUT_ID_MESSAGE);
            return;
        }

        int hideoutId = Integer.parseInt(args[1]);
        hideoutManager.getHideoutById(hideoutId).ifPresentOrElse(
                hideout -> Common.runLater(() -> executor.teleport(hideout.getSpawnLocation())),
                () -> returnTell(HIDEOUT_NOT_FOUND_MESSAGE + hideoutId)
        );
    }

    /**
     * Affiche le hideout en fonction de l'ID fourni.
     *
     * @param executor  Le joueur qui exécute la commande.
     * @param hideoutId L'identifiant du hideout.
     */
    private void showHideoutById(Player executor, int hideoutId) {
        hideoutManager.getHideoutById(hideoutId).ifPresentOrElse(
                hideout -> new HideoutMenu(hideout, plugin).displayTo(executor),
                () -> returnTell(HIDEOUT_NOT_FOUND_MESSAGE + hideoutId)
        );
    }

    /**
     * Gère l'affichage du hideout pour un joueur donné.
     *
     * @param playerName Le nom du joueur.
     * @param executor   Le joueur exécutant la commande.
     */
    private void showHideoutByPlayer(Player executor, String playerName) {
        Player targetPlayer = PlayerUtil.getPlayerByNick(playerName, true);

        UUID targetUUID;
        if (targetPlayer == null) {
            targetUUID = MojangAPI.getUUID(playerName);
            if (targetUUID == null) {
                this.tellError(String.format(PLAYER_NOT_FOUND_MESSAGE, playerName));
                return;
            }
        } else {
            targetUUID = targetPlayer.getUniqueId();
        }

        Gang gang = GangsPlugin.getInstance().getGangManager().getPlayersGang(Bukkit.getOfflinePlayer(targetUUID));
        if (gang == null) {
            this.tellError(String.format(PLAYER_NOT_IN_GANG_MESSAGE, playerName));
            return;
        }

        hideoutManager.getHideoutByGangOwnerId(gang.getId()).ifPresentOrElse(
                hideout -> new HideoutMenu(hideout, plugin).displayTo(executor),
                () -> returnTell(NO_HIDEOUT_FOUND_FOR_PLAYER_MESSAGE + playerName)
        );
    }
}