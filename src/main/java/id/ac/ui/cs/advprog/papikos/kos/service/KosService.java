package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing Kos entities.
 * Defines the business logic operations related to Kos.
 */
public interface KosService {

    /**
     * Creates a new Kos listing associated with a specific owner.
     *
     * @param kos         The Kos object containing the details to be created. ID and timestamps are usually ignored/set during persistence.
     * @param ownerUserId The UUID of the user creating this Kos listing.
     * @return The created Kos object, including its generated ID and timestamps.
     * @throws IllegalArgumentException if the input Kos data is invalid (e.g., null required fields).
     */
    Kos createKos(Kos kos, UUID ownerUserId);

    /**
     * Finds all Kos listings.
     *
     * @return A list of all Kos entities. Returns an empty list if none exist.
     */
    List<Kos> findAllKos();

    /**
     * Finds a specific Kos listing by its unique ID.
     *
     * @param kosId The UUID of the Kos to find.
     * @return The found Kos object.
     * @throws id.ac.ui.cs.advprog.papikos.kos.exception.KosNotFoundException if no Kos with the given ID exists.
     */
    Kos findKosById(UUID kosId);

    /**
     * Finds all Kos listings belonging to a specific owner.
     *
     * @param ownerUserId The UUID of the owner whose Kos listings are to be retrieved.
     * @return A list of Kos entities owned by the specified user. Returns an empty list if the owner has no Kos listings.
     */
    List<Kos> findKosByOwnerUserId(UUID ownerUserId);

    /**
     * Updates an existing Kos listing.
     * Only the owner of the Kos can perform this operation.
     * Fields like ID, ownerUserId, and createdAt are generally not updatable via this method.
     *
     * @param kosId            The UUID of the Kos to update.
     * @param updatedKosData   A Kos object containing the fields to be updated. Null fields in this object might be ignored or handled based on implementation.
     * @param requestingUserId The UUID of the user attempting the update, used for authorization.
     * @return The updated Kos object.
     * @throws id.ac.ui.cs.advprog.papikos.kos.exception.KosNotFoundException        if no Kos with the given ID exists.
     * @throws id.ac.ui.cs.advprog.papikos.kos.exception.UnauthorizedAccessException if the requestingUserId does not match the ownerUserId of the Kos.
     * @throws IllegalArgumentException                                              if the updated data is invalid.
     */
    Kos updateKos(UUID kosId, Kos updatedKosData, UUID requestingUserId);

    /**
     * Deletes a specific Kos listing.
     * Only the owner of the Kos can perform this operation.
     *
     * @param kosId            The UUID of the Kos to delete.
     * @param requestingUserId The UUID of the user attempting the deletion, used for authorization.
     * @throws id.ac.ui.cs.advprog.papikos.kos.exception.KosNotFoundException        if no Kos with the given ID exists.
     * @throws id.ac.ui.cs.advprog.papikos.kos.exception.UnauthorizedAccessException if the requestingUserId does not match the ownerUserId of the Kos.
     */
    void deleteKos(UUID kosId, UUID requestingUserId);

    /**
     * Searches for Kos listings based on a keyword.
     * The search typically looks in fields like name, address, and description.
     *
     * @param keyword The search term.
     * @return A list of Kos entities matching the keyword. Returns an empty list if no matches are found.
     */
    List<Kos> searchKos(String keyword);
}