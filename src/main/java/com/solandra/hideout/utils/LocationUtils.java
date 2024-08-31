package com.solandra.hideout.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.mineacademy.fo.exception.FoException;

import java.util.Objects;

public class LocationUtils {
    /**
     * Désérialise une chaîne de caractères en un objet {@link Location}.
     *
     * @param locationStr La chaîne de caractères représentant la {@link Location}, au format "world:x:y:z:yaw:pitch".
     * @return Un objet {@link Location} correspondant aux coordonnées et au monde spécifiés.
     * @throws FoException Si la chaîne de caractères est invalide ou si le monde spécifié n'existe pas.
     */
    public static Location deserialize(String locationStr) {
        if (locationStr == null) {
            throw new FoException("Location spécifiée invalide.");
        }

        String[] components = locationStr.split(":");
        if (components.length < 6) {
            throw new FoException("Location spécifiée invalide.");
        }

        World world = Bukkit.getWorld(components[0]);
        if (world == null) {
            throw new FoException("World spécifié invalide.");
        }

        return new Location(
                world,
                Double.parseDouble(components[1]),
                Double.parseDouble(components[2]),
                Double.parseDouble(components[3]),
                Float.parseFloat(components[4]),
                Float.parseFloat(components[5])
        );
    }

    /**
     * Sérialise un objet {@link Location} en une chaîne de caractères.
     *
     * @param location L'objet {@link Location} à sérialiser.
     * @return Une chaîne de caractères représentant la {@link Location}, au format "world:x:y:z:yaw:pitch".
     *         Retourne une chaîne vide si l'objet {@link Location} est null.
     */
    public static String serialize(Location location) {
        if (location == null) {
            return "";
        }

        return Objects.requireNonNull(location.getWorld()).getName() + ":" +
                location.getX() + ":" +
                location.getY() + ":" +
                location.getZ() + ":" +
                location.getYaw() + ":" +
                location.getPitch();
    }
}