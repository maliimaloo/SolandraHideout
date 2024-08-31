package com.solandra.hideout;

import com.solandra.hideout.api.HideoutAPI;
import com.solandra.hideout.api.MineAPI;
import com.solandra.hideout.database.HideoutDatabase;
import com.solandra.hideout.manager.HideoutManager;
import com.solandra.hideout.manager.MineManager;
import com.solandra.hideout.bootstrap.PluginInitializer;
import com.solandra.hideout.bootstrap.StartupLogoLoader;
import com.solandra.hideout.bootstrap.DatabaseHandler;
import com.solandra.prisoncore.api.PlayerAPI;
import org.mineacademy.fo.plugin.SimplePlugin;

public class Main extends SimplePlugin {
    // API
    private static HideoutAPI hideoutAPI;
    private static MineAPI mineAPI;

    private PlayerAPI playerAPI;
    private HideoutDatabase hideoutDatabase;
    private HideoutManager hideoutManager;
    private MineManager mineManager;

    @Override
    protected void onPluginLoad() {
        PluginInitializer.initializeFields();
    }

    @Override
    protected void onPluginStart() {
        PluginInitializer.initializeAPI();
        PluginInitializer.initializeListeners();
    }

    @Override
    protected void onPluginStop() {
        DatabaseHandler.cleanupPlugin();
    }

    @Override
    protected String[] getStartupLogo() {
        return StartupLogoLoader.loadLogo();
    }

    public PlayerAPI getPlayerAPI() {
        return playerAPI;
    }

    public void setPlayerAPI(PlayerAPI playerAPI) {
        this.playerAPI = playerAPI;
    }

    public HideoutDatabase getHideoutDatabase() {
        return hideoutDatabase;
    }

    public void setHideoutDatabase(HideoutDatabase hideoutDatabase) {
        this.hideoutDatabase = hideoutDatabase;
    }

    public HideoutManager getHideoutManager() {
        return hideoutManager;
    }

    public void setHideoutManager(HideoutManager hideoutManager) {
        this.hideoutManager = hideoutManager;
    }

    public MineManager getMineManager() {
        return mineManager;
    }

    public void setMineManager(MineManager mineManager) {
        this.mineManager = mineManager;
    }

    public static HideoutAPI getHideoutAPI() {
        return hideoutAPI;
    }

    public static void setHideoutAPI(HideoutAPI hideoutAPI) {
        Main.hideoutAPI = hideoutAPI;
    }

    public static MineAPI getMineAPI() {
        return mineAPI;
    }

    public static void setMineAPI(MineAPI mineAPI) {
        Main.mineAPI = mineAPI;
    }

    public static Main getInstance() {
        return (Main) SimplePlugin.getInstance();
    }
}