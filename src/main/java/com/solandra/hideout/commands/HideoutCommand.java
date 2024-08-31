package com.solandra.hideout.commands;

import com.solandra.hideout.Main;
import com.solandra.hideout.manager.HideoutManager;
import com.solandra.hideout.menu.HideoutMenu;
import net.brcdev.gangs.GangsPlusApi;
import net.brcdev.gangs.gang.Gang;
import org.bukkit.entity.Player;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.command.SimpleCommand;

@AutoRegister
public final class HideoutCommand extends SimpleCommand {
    // Constantes pour les messages et sous-commandes
    private static final String NO_GANG_MESSAGE = "Vous devez être dans un gang pour faire cela.";
    private static final String HIDEOUT_NOT_FOUND_MESSAGE = "&cAucun hideout trouvé pour l'id: ";

    // Fields
    private final Main plugin = Main.getInstance();
    private final HideoutManager hideoutManager = Main.getInstance().getHideoutManager();

    /**
     * Constructeur de la commande HideoutCommand.
     * Initialise le gestionnaire de repaires (HideoutManager) à partir du plugin principal.
     */
    public HideoutCommand() {
        super("hideout|ho");
    }

    /**
     * Méthode principale exécutée lorsque la commande /hideout ou /ho est utilisée.
     * Vérifie que la commande n'est pas exécutée depuis la console et gère les arguments.
     */
    @Override
    protected void onCommand() {
        checkConsole();
        Player executor = getPlayer();

        handleNoArguments(executor);
    }

    /**
     * Gère le cas où la commande est exécutée sans aucun argument.
     * Vérifie si le joueur fait partie d'un gang, puis affiche le menu du repaire associé,
     * ou renvoie un message d'erreur si aucun repaire n'est trouvé.
     *
     * @param executor Le joueur qui a exécuté la commande.
     */
    private void handleNoArguments(Player executor) {
        if (!GangsPlusApi.isInGang(executor)) {
            tellError(NO_GANG_MESSAGE);
            return;
        }

        Gang gang = GangsPlusApi.getPlayersGang(executor);
        int gangId = gang.getId();

        hideoutManager.getHideoutByGangOwnerId(gangId).ifPresentOrElse(
                hideout -> new HideoutMenu(hideout, plugin).displayTo(executor),
                () -> tellError(HIDEOUT_NOT_FOUND_MESSAGE + gangId + ".")
        );
    }
}