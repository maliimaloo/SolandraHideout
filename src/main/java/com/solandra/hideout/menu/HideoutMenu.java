package com.solandra.hideout.menu;

import com.solandra.hideout.Main;
import com.solandra.hideout.manager.HideoutManager;
import com.solandra.hideout.manager.MineManager;
import com.solandra.hideout.model.Hideout;
import net.brcdev.gangs.GangsPlugin;
import net.brcdev.gangs.GangsPlusApi;
import net.brcdev.gangs.gang.Gang;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.ChatUtil;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.MenuClickLocation;
import org.mineacademy.fo.remain.CompMaterial;

import java.util.Objects;

/**
 * Menu pour gérer les actions et les informations du hideout.
 */
public class HideoutMenu extends Menu {
    private final Main plugin;

    private final HideoutManager hideoutManager;
    private final MineManager mineManager;

    private final Hideout hideout;
    private final Button teleportButton;
    private final Button disbandButton;
    private final Button infoButton;
    private final Button upgradeButton;

    /**
     * Constructeur pour initialiser le menu du hideout avec les boutons d'action.
     *
     * @param hideout Le hideout auquel ce menu est associé.
     */
    public HideoutMenu(Hideout hideout, Main plugin) {
        this.plugin = plugin;

        this.hideoutManager = plugin.getHideoutManager();
        this.mineManager = plugin.getMineManager();
        this.hideout = hideout;

        setTitle("Grand coffre");
        setSize(4 * 9);

        this.teleportButton = createTeleportButton();
        this.disbandButton = createDisbandButton();
        this.infoButton = createInfoButton();
        this.upgradeButton = createUpgradeButton();
    }

    /**
     * Obtient l'élément à afficher à un emplacement spécifique du menu.
     *
     * @param slot L'emplacement dans l'inventaire.
     * @return L'élément à afficher.
     */
    @Override
    public ItemStack getItemAt(int slot) {
        return switch (slot) {
            case 13 -> infoButton.getItem();
            case 19 -> teleportButton.getItem();
            case 20 -> upgradeButton.getItem();
            case 25 -> disbandButton.getItem();
            default -> ItemCreator.of(CompMaterial.WHITE_STAINED_GLASS_PANE).name(" ").make();
        };
    }

    /**
     * Crée le bouton d'information affichant les détails du hideout.
     *
     * @return Le bouton d'information.
     */
    private Button createInfoButton() {
        return new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType clickType) {
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(CompMaterial.PAINTING)
                        .name("&eInformations du Hideout")
                        .lore(
                                "",
                                "&eID: &f" + hideout.getId(),
                                "&eGang Owner ID: &f" + hideout.getGangOwnerId(),
                                ChatUtil.center("&e-----[Main Region-----]"),
                                "&e- Primary: &f" + formatLocation(hideout.getMainRegion().getPrimary()),
                                "&e- Secondary: &f" + formatLocation(hideout.getMainRegion().getSecondary()),
                                ChatUtil.center("&e-----[Mine Region-----]"),
                                "&e- Primary: &f" + formatLocation(hideout.getMine().getRegion().getPrimary()),
                                "&e- Secondary: &f" + formatLocation(hideout.getMine().getRegion().getSecondary()),
                                "",
                                "&ePrestige Total : " + hideout.getPrestige()
                        )
                        .make();
            }
        };
    }


    /**
     * Crée le bouton de téléportation au centre du hideout.
     *
     * @return Le bouton de téléportation.
     */
    private Button createTeleportButton() {
        return new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType clickType) {
                handleTeleport(player);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(CompMaterial.COMPASS)
                        .name("&eTéléportation")
                        .lore("Cliquez pour vous téléporter", "au centre du hideout.")
                        .make();
            }
        };
    }

    private Button createUpgradeButton() {
        return new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType clickType) {
                new HideoutUpgradeMenu(hideout, plugin).displayTo(player);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(CompMaterial.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                        .name("&eUpgrade Hideout")
                        .lore("Cliquez pour améliorer", "votre hideout.")
                        .make();
            }
        };
    }

    /**
     * Crée le bouton pour dissoudre le hideout.
     *
     * @return Le bouton de dissociation du hideout.
     */
    private Button createDisbandButton() {
        return new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType clickType) {
                handleDisband(player);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(CompMaterial.RED_WOOL)
                        .name("&cDissoudre le hideout")
                        .lore("Cliquez pour dissoudre", "le hideout.")
                        .make();
            }
        };
    }

    /**
     * Détermine si une action est autorisée dans le menu.
     *
     * @param location Le lieu du clic dans le menu.
     * @param slot     L'emplacement du clic.
     * @param clicked  L'objet cliqué.
     * @param cursor   L'objet en cours de survol.
     * @return False pour désactiver toutes les autres actions dans le menu.
     */
    @Override
    protected boolean isActionAllowed(MenuClickLocation location, int slot, ItemStack clicked, ItemStack cursor) {
        return false; // Désactiver toutes les autres actions dans le menu
    }

    private void handleTeleport(Player player) {
        player.teleport(hideout.getSpawnLocation());
        Messenger.success(player, "Vous avez été téléporté à votre hideout !");
    }

    private void handleDisband(Player player) {
        Gang gangExecutor = GangsPlusApi.getPlayersGang(player);
        if (!gangExecutor.getOwner().getUniqueId().equals(player.getUniqueId())) {
            Messenger.error(player, "Vous devez être owner de votre gang pour faire cela.");
            return;
        }

        hideoutManager.getHideoutByGangOwnerId(gangExecutor.getId()).ifPresentOrElse(hideout -> {
            hideout.setGangOwnerId(0);
            hideoutManager.updateHideout(hideout);
            player.closeInventory();

            hideoutManager.placeSchematicAtRegion(hideout.getMainRegion()).thenRun(() -> mineManager.resetMine(hideout));
        }, () -> this.tellError("Aucun hideout trouvé."));

        GangsPlugin.getInstance().getGangManager().removeGang(gangExecutor);
    }

    private String formatLocation(Location location) {
        return String.format(
                "Monde: %s, x: %.2f, y: %.2f, z: %.2f",
                Objects.requireNonNull(location.getWorld()).getName(),
                location.getX(),
                location.getY(),
                location.getZ()
        );
    }
}