package com.solandra.hideout.api;

import com.solandra.hideout.model.Hideout;
import org.mineacademy.fo.region.Region;

import java.util.AbstractMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Interface API pour la gestion des hideouts.
 * Fournit des méthodes pour créer, récupérer, mettre à jour et manipuler les hideouts.
 */
public interface HideoutAPI {

    /**
     * Charge ou crée un hideout pour un gang.
     *
     * @param gangOwnerId L'identifiant du propriétaire du gang.
     * @return Un CompletableFuture contenant un SimpleEntry avec le hideout chargé ou créé, et un boolean indiquant s'il est nouveau.
     */
    CompletableFuture<AbstractMap.SimpleEntry<Hideout, Boolean>> loadOrCreateHideoutForGang(int gangOwnerId);

    /**
     * Charge tous les hideouts en mémoire.
     */
    void loadAllHideouts();

    /**
     * Récupère un hideout par l'identifiant du gang propriétaire.
     *
     * @param gangOwnerId L'identifiant du gang.
     * @return Un Optional contenant le hideout s'il est trouvé.
     */
    Optional<Hideout> getHideoutByGangOwnerId(int gangOwnerId);

    /**
     * Récupère un hideout par son identifiant.
     *
     * @param id L'identifiant du hideout.
     * @return Un Optional contenant le hideout s'il est trouvé.
     */
    Optional<Hideout> getHideoutById(int id);

    /**
     * Ajoute un nouveau hideout.
     *
     * @param hideout Le hideout à ajouter.
     * @return Un CompletableFuture contenant le hideout ajouté.
     */
    CompletableFuture<Hideout> addHideout(Hideout hideout);

    /**
     * Met à jour un hideout existant.
     *
     * @param hideout Le hideout à mettre à jour.
     */
    void updateHideout(Hideout hideout);

    /**
     * Récupère tous les identifiants des hideouts.
     *
     * @return Une liste des identifiants de tous les hideouts.
     */
    List<Integer> getAllHideoutIds();

    /**
     * Place une schématique dans la région spécifiée.
     *
     * @param region La région où placer la schématique.
     * @return Un CompletableFuture indiquant la fin de l'opération.
     */
    CompletableFuture<Void> placeSchematicAtRegion(Region region);

    /**
     * Calcule un nouveau Hideout.
     *
     * @return Un CompletableFuture contenant le nouveau Hideout.
     */
    CompletableFuture<Hideout> calculateNewHideout();
}