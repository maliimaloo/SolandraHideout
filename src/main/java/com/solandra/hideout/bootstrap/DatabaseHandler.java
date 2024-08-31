package com.solandra.hideout.bootstrap;

import com.solandra.hideout.Main;
import com.solandra.hideout.utils.SystemPrint;

public class DatabaseHandler {
    public static void cleanupPlugin() {
        if (Main.getInstance().getHideoutDatabase().isLoaded()) {
            SystemPrint.debugDB("Déconnexion de la base de donnees.");
            Main.getInstance().getHideoutDatabase().close();
        }
    }
}
