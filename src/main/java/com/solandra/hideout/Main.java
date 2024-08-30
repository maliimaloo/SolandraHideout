package com.solandra.hideout;

import com.solandra.hideout.api.HideoutAPI;
import com.solandra.hideout.api.HideoutAPIImplementation;
import com.solandra.hideout.api.MineAPI;
import com.solandra.hideout.api.MineAPIImplementation;
import com.solandra.hideout.database.HideoutDatabase;
import com.solandra.hideout.manager.HideoutManager;
import com.solandra.hideout.manager.MineManager;
import com.solandra.hideout.utils.SystemPrint;
import com.solandra.prisoncore.Core;
import com.solandra.prisoncore.api.PlayerAPI;
import org.mineacademy.fo.FileUtil;
import org.mineacademy.fo.plugin.SimplePlugin;

import java.io.File;
import java.util.List;

public class Main extends SimplePlugin {
    // API
    private static HideoutAPI hideoutAPI;
    private static MineAPI mineAPI;

    // Constantes
    private static final String FILE_LOGO_PATH = "logo.txt";

    // Fields
    private PlayerAPI playerAPI;
    private HideoutDatabase hideoutDatabase;
    private HideoutManager hideoutManager;
    private MineManager mineManager;
    

    public HideoutDatabase getHideoutDatabase() {
        return hideoutDatabase;
    }

    public HideoutManager getHideoutManager() {
        return hideoutManager;
    }

    public MineManager getMineManager() {
        return mineManager;
    }

    public PlayerAPI getPlayerAPI() {
        return playerAPI;
    }

    @Override
    protected void onPluginLoad() {
        initializeField();
    }

    @Override
    protected void onPluginStart() {
        initializeListeners();
        initializeAPI();
    }

    @Override
    protected void onReloadablesStart() {

    }

    @Override
    protected void onPluginStop() {
        cleanupPlugin();
    }

    @Override
    protected String[] getStartupLogo() {
        File file = FileUtil.extract(FILE_LOGO_PATH);
        List<String> lines = FileUtil.readLines(file);

        String debugMessageDB;
        if (hideoutDatabase.isLoaded()) {
            debugMessageDB = SystemPrint.debugColorDB("Connexion établie avec la base de données SQLite.");
        } else {
            debugMessageDB = SystemPrint.debugColorDB("\u001b[38;5;9mConnexion échouer avec la base de données SQLite.");
        }

        lines.add(debugMessageDB);
        return lines.toArray(new String[0]);
    }

    private void cleanupPlugin() {
        if (hideoutDatabase.isLoaded()) {
            SystemPrint.debugDB("Déconnexion de la base de donnees.");
            hideoutDatabase.close();
        }
    }

    private void initializeField() {
        this.playerAPI = Core.getPlayerAPI();
        this.hideoutDatabase = new HideoutDatabase();
        this.hideoutManager = new HideoutManager(this);
        this.mineManager = new MineManager(this);
    }

    private void initializeListeners() {

    }

    private void initializeAPI() {
        hideoutAPI = new HideoutAPIImplementation(this);
        mineAPI = new MineAPIImplementation(this);
    }

    public static HideoutAPI getHideoutAPI() {
        return hideoutAPI;
    }

    public static MineAPI getMineAPI() {
        return mineAPI;
    }

    public static Main getInstance() {
        return (Main) SimplePlugin.getInstance();
    }
}
