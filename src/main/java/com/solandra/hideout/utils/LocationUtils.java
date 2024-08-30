package com.solandra.hideout.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.mineacademy.fo.exception.FoException;

import java.util.Objects;

public class LocationUtils {
    public LocationUtils() {
    }

    public static Location deserialize(String locationStr) {
        if (locationStr == null) {
            throw new FoException("Location spécifié invalide.");
        } else {
            String[] var1 = locationStr.split(":");
            if (var1.length < 6) {
                throw new FoException("Location spécifié invalide.");
            } else {
                World var2 = Bukkit.getWorld(var1[0]);
                if (var2 == null) {
                    throw new FoException("World spécifié invalide.");
                } else {
                    return new Location(var2, Double.parseDouble(var1[1]), Double.parseDouble(var1[2]), Double.parseDouble(var1[3]), Float.parseFloat(var1[4]), Float.parseFloat(var1[5]));
                }
            }
        }
    }

    public static String serialize(Location location) {
        return location == null ? "" : Objects.requireNonNull(location.getWorld()).getName() + ":" + location.getX() + ":" + location.getY() + ":" + location.getZ() + ":" + location.getYaw() + ":" + location.getPitch();
    }
}
