package com.solandra.hideout.menu;

import com.solandra.hideout.Main;
import com.solandra.hideout.manager.MineManager;
import com.solandra.hideout.model.Hideout;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.MenuClickLocation;
import org.mineacademy.fo.remain.CompMaterial;

public class HideoutUpgradeMenu extends Menu {

    private final Hideout hideout;
    private final MineManager mineManager;
    private final Button upgradeMineButton;

    /**
     * Constructeur pour initialiser le menu d'upgrade du hideout.
     *
     * @param hideout Le hideout auquel ce menu est associé.
     */
    public HideoutUpgradeMenu(Hideout hideout, Main plugin) {
        this.hideout = hideout;
        this.mineManager = plugin.getMineManager();

        setTitle("Upgrade Hideout");
        setSize(3 * 9);

        this.upgradeMineButton = createUpgradeMineButton();
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
            case 12 -> upgradeMineButton.getItem();
            default -> ItemCreator.of(CompMaterial.WHITE_STAINED_GLASS_PANE).name(" ").make();
        };
    }

    /**
     * Crée le bouton pour l'upgrade de la mine.
     *
     * @return Le bouton pour l'upgrade de la mine.
     */
    private Button createUpgradeMineButton() {
        return new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType clickType) {
                mineManager.upgradeMine(player, hideout);
                player.closeInventory();
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(CompMaterial.DIAMOND_ORE)
                        .name("&eUpgrade Mine")
                        .lore("Cliquez pour améliorer la mine.")
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
}