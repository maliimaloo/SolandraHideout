package com.solandra.hideout.utils;

import org.mineacademy.fo.Common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class MojangAPI {

    private static final String MOJANG_API_URL = "https://api.mojang.com/users/profiles/minecraft/";

    /**
     * Récupère l'UUID d'un joueur Minecraft à partir de son nom.
     *
     * @param playerName Le nom du joueur.
     * @return L'UUID du joueur, ou null s'il n'a pas été trouvé.
     */
    public static UUID getUUID(String playerName) {
        try {
            URL url = new URL(MOJANG_API_URL + playerName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            if (response.isEmpty()) {
                return null;
            }

            String jsonResponse = response.toString();
            String uuidString = jsonResponse.split("\"id\":\"")[1].split("\"")[0];

            // Ajouter des tirets pour obtenir le format UUID standard
            String formattedUUID = uuidString.replaceFirst(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                    "$1-$2-$3-$4-$5"
            );

            return UUID.fromString(formattedUUID);
        } catch (Exception e) {
            Common.throwError(e, "Impossible de récupérer l'uuid du joueur " + playerName);
            return null;
        }
    }
}