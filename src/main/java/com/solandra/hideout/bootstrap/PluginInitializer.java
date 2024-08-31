package com.solandra.hideout.bootstrap;

import com.solandra.hideout.Main;
import com.solandra.hideout.api.HideoutAPIImplementation;
import com.solandra.hideout.api.MineAPIImplementation;
import com.solandra.hideout.database.HideoutDatabase;
import com.solandra.hideout.manager.HideoutManager;
import com.solandra.hideout.manager.MineManager;
import com.solandra.prisoncore.Core;

public class PluginInitializer {
    public static void initializeFields() {
        Main.getInstance().setPlayerAPI(Core.getPlayerAPI());
        Main.getInstance().setHideoutDatabase(new HideoutDatabase());
        Main.getInstance().setHideoutManager(new HideoutManager());
        Main.getInstance().setMineManager(new MineManager());
    }

    public static void initializeAPI() {
        Main.setHideoutAPI(new HideoutAPIImplementation(Main.getInstance()));
        Main.setMineAPI(new MineAPIImplementation(Main.getInstance()));
    }

    public static void initializeListeners() {
        // Add listeners initialization here
    }
}