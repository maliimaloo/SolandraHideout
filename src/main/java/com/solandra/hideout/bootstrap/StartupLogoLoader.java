package com.solandra.hideout.bootstrap;

import com.solandra.hideout.Main;
import com.solandra.hideout.utils.SystemPrint;
import org.mineacademy.fo.FileUtil;

import java.io.File;
import java.util.List;

public class StartupLogoLoader {
    private static final String FILE_LOGO_PATH = "logo.txt";

    public static String[] loadLogo() {
        File file = FileUtil.extract(FILE_LOGO_PATH);
        List<String> lines = FileUtil.readLines(file);

        String debugMessageDB;
        if (Main.getInstance().getHideoutDatabase().isLoaded()) {
            debugMessageDB = SystemPrint.debugColorDB("Connexion établie avec la base de données SQLite.");
        } else {
            debugMessageDB = SystemPrint.debugColorDB("\u001b[38;5;9mConnexion échouer avec la base de données SQLite.");
        }

        lines.add(debugMessageDB);
        return lines.toArray(new String[0]);
    }
}