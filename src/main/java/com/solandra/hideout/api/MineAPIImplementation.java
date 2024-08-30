package com.solandra.hideout.api;

import com.solandra.hideout.Main;
import com.solandra.hideout.manager.MineManager;
import com.solandra.hideout.model.Hideout;
import org.bukkit.entity.Player;
import org.mineacademy.fo.region.Region;

import java.util.concurrent.CompletableFuture;

/**
 * Implémentation de l'API Mine.
 * Utilise les classes Mine et MineManager pour gérer les opérations sur les mines.
 */
public class MineAPIImplementation implements MineAPI {

    private final MineManager mineManager;

    /**
     * Constructeur pour MineAPIImplementation.
     */
    public MineAPIImplementation(Main plugin) {
        this.mineManager = plugin.getMineManager();
    }

    @Override
    public void upgradeMine(Player executor, Hideout hideout) {
        mineManager.upgradeMine(executor, hideout);
    }

    @Override
    public void resetMine(Hideout hideout) {
        mineManager.resetMine(hideout);
    }

    @Override
    public CompletableFuture<Void> fillWithBlocks(Hideout hideout) {
        return mineManager.fillWithBlocks(hideout);
    }

    @Override
    public Region getMineRegion(Hideout hideout) {
        return hideout.getMine().getRegion();
    }

    @Override
    public int getMineLevel(Hideout hideout) {
        return hideout.getMine().getLevel();
    }

    @Override
    public void setMineLevel(Hideout hideout, int level) {
        hideout.getMine().setLevel(level);
    }

    @Override
    public void addMineLevel(Hideout hideout) {
        hideout.getMine().addLevel();
    }

    @Override
    public void setMineRegion(Hideout hideout, Region region) {
        hideout.getMine().setMineRegion(region);
    }
}