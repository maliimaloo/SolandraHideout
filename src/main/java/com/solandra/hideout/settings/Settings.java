package com.solandra.hideout.settings;

import org.mineacademy.fo.settings.SimpleSettings;

import java.util.List;

public final class Settings extends SimpleSettings {
    public static String LOG_PREFIX;
    public static List<String> ALIASES;

    private static void init() {
        LOG_PREFIX = getString("Log_Prefix");
        ALIASES = getStringList("Aliases");
    }
}
