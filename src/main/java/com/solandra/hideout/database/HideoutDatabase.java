package com.solandra.hideout.database;

import com.solandra.hideout.model.Hideout;
import com.solandra.hideout.model.Mine;
import com.solandra.hideout.utils.LocationUtils;
import org.bukkit.Location;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.FileUtil;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.database.SimpleDatabase;
import org.mineacademy.fo.region.Region;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Gère la base de données des hideouts, utilisant SQLite pour stocker les informations.
 * Fournit des méthodes pour ajouter, mettre à jour, supprimer et récupérer des hideouts.
 */
public class HideoutDatabase extends SimpleDatabase {

    /**
     * Initialise la base de données des hideouts et établit une connexion à la base de données SQLite.
     */
    public HideoutDatabase() {
        File dataFile = FileUtil.getOrMakeFile("/database/hideout.db");
        this.connect("jdbc:sqlite:" + dataFile.getPath());
    }

    @Override
    protected void onConnected() {
        createTable();
    }

    /**
     * Crée la table "hideouts" dans la base de données si elle n'existe pas déjà.
     */
    private void createTable() {
        createTable(TableCreator.of("hideouts")
                .addAutoIncrement("id", "INTEGER")
                .add("mainRegion", "TEXT")
                .add("gangOwnerId", "INTEGER")
                .add("spawnLocation", "TEXT")
                .add("mineData", "TEXT")
                .setPrimaryColumn("id"));
    }

    /**
     * Ajoute un nouveau hideout à la base de données de manière asynchrone et retourne son identifiant généré.
     *
     * @param hideout Le hideout à ajouter.
     * @return Un CompletableFuture contenant l'identifiant du hideout ajouté.
     */
    public CompletableFuture<Integer> addHideout(Hideout hideout) {
        return CompletableFuture.supplyAsync(() -> {
            SerializedMap serializedHideout = serializeHideout(hideout);
            insert("hideouts", serializedHideout);
            return getLastInsertedId();
        });
    }

    /**
     * Met à jour un hideout existant dans la base de données de manière asynchrone.
     *
     * @param hideout Le hideout à mettre à jour.
     */
    public void updateHideout(Hideout hideout) {
        CompletableFuture.runAsync(() -> {
            SerializedMap serializedHideout = serializeHideout(hideout);

            String sql = String.format(
                    "UPDATE hideouts SET mainRegion = '%s', gangOwnerId = %d, spawnLocation = '%s', mineData = '%s' WHERE id = %d;",
                    serializedHideout.getString("mainRegion"),
                    serializedHideout.getInteger("gangOwnerId"),
                    serializedHideout.getString("spawnLocation"),
                    serializedHideout.getString("mineData"),
                    hideout.getId()
            );

            update(sql);
        });
    }

    /**
     * Supprime un hideout de la base de données de manière asynchrone.
     *
     * @param hideoutId L'identifiant du hideout à supprimer.
     */
    public void removeHideout(int hideoutId) {
        CompletableFuture.runAsync(() -> {
            String sql = String.format("DELETE FROM hideouts WHERE id = %d;", hideoutId);
            update(sql);
        });
    }

    /**
     * Récupère tous les hideouts de la base de données de manière asynchrone.
     *
     * @return Un CompletableFuture contenant la liste de tous les hideouts.
     */
    public CompletableFuture<List<Hideout>> getAllHideouts() {
        return CompletableFuture.supplyAsync(() -> {
            List<Hideout> hideouts = new ArrayList<>();

            selectAll("hideouts", resultSet -> {
                try {
                    Hideout hideout = deserializeHideout(resultSet);
                    hideouts.add(hideout);
                } catch (SQLException exception) {
                    Common.throwError(exception, "Erreur lors de la désérialisation du hideout à partir du ResultSet.");
                }
            });

            return hideouts;
        });
    }

    /**
     * Récupère le dernier hideout inséré dans la base de données de manière asynchrone.
     *
     * @return Un CompletableFuture contenant le dernier hideout inséré, ou un Optional.empty() si aucun hideout n'est trouvé.
     */
    public CompletableFuture<Optional<Hideout>> getLastHideout() {
        return CompletableFuture.supplyAsync(() -> {
            final Hideout[] hideout = new Hideout[1];
            select("hideouts", "1 ORDER BY id DESC LIMIT 1", resultSet -> hideout[0] = deserializeHideout(resultSet));
            return Optional.ofNullable(hideout[0]);
        });
    }

    /**
     * Récupère l'identifiant du dernier hideout inséré dans la base de données.
     *
     * @return L'identifiant du dernier hideout inséré.
     */
    private int getLastInsertedId() {
        final int[] lastInsertedId = {0};
        select("hideouts", "1 ORDER BY id DESC LIMIT 1", resultSet -> lastInsertedId[0] = resultSet.getInt("id"));
        return lastInsertedId[0];
    }

    /**
     * Désérialise un hideout à partir d'un ResultSet.
     *
     * @param resultSet Le ResultSet contenant les données du hideout.
     * @return Le hideout désérialisé.
     * @throws SQLException Si une erreur survient lors de la désérialisation.
     */
    private Hideout deserializeHideout(SimpleResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        Region mainRegion = Region.deserialize(SerializedMap.fromJson(resultSet.getString("mainRegion")));
        int gangOwnerId = resultSet.getInt("gangOwnerId");
        Location spawnLocation = LocationUtils.deserialize(resultSet.getString("spawnLocation"));

        SerializedMap mineDataMap = SerializedMap.fromJson(resultSet.getString("mineData"));
        Mine mine = Mine.deserialize(mineDataMap);

        return new Hideout(id, mainRegion, mine, spawnLocation, gangOwnerId);
    }

    /**
     * Sérialise un hideout en un SerializedMap pour l'insertion ou la mise à jour dans la base de données.
     *
     * @param hideout Le hideout à sérialiser.
     * @return Le SerializedMap contenant les données sérialisées du hideout.
     */
    private SerializedMap serializeHideout(Hideout hideout) {
        SerializedMap serializedHideout = new SerializedMap();
        serializedHideout.put("mainRegion", hideout.getMainRegion().serialize().toJson());
        serializedHideout.put("gangOwnerId", hideout.getGangOwnerId());
        serializedHideout.put("spawnLocation", LocationUtils.serialize(hideout.getSpawnLocation()));
        serializedHideout.put("mineData", hideout.getMine().serialize().toJson());

        return serializedHideout;
    }
}