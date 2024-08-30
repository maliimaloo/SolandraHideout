package com.solandra.hideout.api;

import com.solandra.hideout.Main;
import com.solandra.hideout.manager.HideoutManager;
import com.solandra.hideout.model.Hideout;
import org.mineacademy.fo.region.Region;

import java.util.AbstractMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Implémentation de l'API Hideout.
 * Utilise les classes HideoutManager et HideoutDatabase pour gérer les opérations sur les hideouts.
 */
public class HideoutAPIImplementation implements HideoutAPI {

    private final HideoutManager hideoutManager;

    /**
     * Constructeur pour HideoutAPIImplementation.
     */
    public HideoutAPIImplementation(Main plugin) {
        this.hideoutManager = plugin.getHideoutManager();
    }

    @Override
    public CompletableFuture<AbstractMap.SimpleEntry<Hideout, Boolean>> loadOrCreateHideoutForGang(int gangOwnerId) {
        return hideoutManager.loadOrCreateHideoutForGang(gangOwnerId);
    }

    @Override
    public void loadAllHideouts() {
        hideoutManager.loadAllHideouts();
    }

    @Override
    public Optional<Hideout> getHideoutByGangOwnerId(int gangOwnerId) {
        return hideoutManager.getHideoutByGangOwnerId(gangOwnerId);
    }

    @Override
    public Optional<Hideout> getHideoutById(int id) {
        return hideoutManager.getHideoutById(id);
    }

    @Override
    public CompletableFuture<Hideout> addHideout(Hideout hideout) {
        return hideoutManager.addHideout(hideout);
    }

    @Override
    public void updateHideout(Hideout hideout) {
        hideoutManager.updateHideout(hideout);
    }

    @Override
    public List<Integer> getAllHideoutIds() {
        return hideoutManager.getAllHideoutIds();
    }

    @Override
    public CompletableFuture<Void> placeSchematicAtRegion(Region region) {
        return hideoutManager.placeSchematicAtRegion(region);
    }

    @Override
    public CompletableFuture<Hideout> calculateNewHideout() {
        return hideoutManager.calculateNewHideout();
    }
}