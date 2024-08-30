package com.solandra.hideout.utils;

import org.mineacademy.fo.Common;

public class SystemPrint {
    public static void debug(String message) {
        Common.log("\u001b[38;5;2m[SolandraHideout] \u001B[38;5;220m" + message + "\u001B[0m");
    }

    public static void debugDB(String message) {
        Common.log("\u001b[38;5;2m[SolandraHideoutDB] \u001B[38;5;220m" + message + "\u001B[0m");
    }

    public static String debugColor(String message) {
        return "\u001b[38;5;2m[SolandraHideout] \u001B[38;5;220m" + message + "\u001B[0m";
    }

    public static String debugColorDB(String message) {
        return "\u001b[38;5;2m[SolandraHideoutDB] \u001B[38;5;220m" + message + "\u001B[0m";
    }
}
