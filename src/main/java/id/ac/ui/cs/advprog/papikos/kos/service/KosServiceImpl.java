package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.kos.exception.KosNotFoundException;
import id.ac.ui.cs.advprog.papikos.kos.exception.UnauthorizedAccessException;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.repository.KosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KosServiceImpl implements KosService {

    private final KosRepository kosRepository;

    @Override
    @Transactional
    public Kos createKos(Kos kos, UUID ownerUserId) {
        validateKosInput(kos, true);

        kos.setOwnerUserId(ownerUserId);
        return kosRepository.save(kos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Kos> findAllKos() {
        return kosRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Kos findKosById(UUID kosId) {
        return kosRepository.findById(kosId)
                .orElseThrow(() -> new KosNotFoundException(kosId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Kos> findKosByOwnerUserId(UUID ownerUserId) {
        return kosRepository.findKosByOwnerUserId(ownerUserId);
    }

    @Override
    @Transactional
    public Kos updateKos(UUID kosId, Kos updatedKosData, UUID requestingUserId) {
        Kos existingKos = findKosById(kosId);

        if (!existingKos.getOwnerUserId().equals(requestingUserId)) {
            throw new UnauthorizedAccessException("User " + requestingUserId + " is not authorized to update Kos " + kosId);
        }

        // Validate incoming update data (optional checks for non-null values if required)
        validateKosInput(updatedKosData, false); // Don't require all fields for update

        // Apply updates from updatedKosData to existingKos
        // Only update fields that are allowed to change and are provided
        if (updatedKosData.getName() != null) {
            existingKos.setName(updatedKosData.getName());
        }
        if (updatedKosData.getAddress() != null) {
            existingKos.setAddress(updatedKosData.getAddress());
        }
        // Allow description to be set to null or empty if intended
        existingKos.setDescription(updatedKosData.getDescription());

        if (updatedKosData.getNumRooms() != null) {
            existingKos.setNumRooms(updatedKosData.getNumRooms());
        }
        if (updatedKosData.getMonthlyRentPrice() != null) {
            existingKos.setMonthlyRentPrice(updatedKosData.getMonthlyRentPrice());
        }
        if (updatedKosData.getIsListed() != null) {
            existingKos.setIsListed(updatedKosData.getIsListed());
        }

        return kosRepository.save(existingKos);
    }

    @Override
    @Transactional
    public void deleteKos(UUID kosId, UUID requestingUserId) {
        Kos kosToDelete = findKosById(kosId); // Throws KosNotFoundException if not found

        if (!kosToDelete.getOwnerUserId().equals(requestingUserId)) {
            throw new UnauthorizedAccessException("User " + requestingUserId + " is not authorized to delete Kos " + kosId);
        }

        kosRepository.deleteById(kosId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Kos> searchKos(String keyword) {
        return kosRepository.findKosByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                keyword, keyword, keyword
        );
    }

    /**
     * Helper method for basic validation of Kos data.
     *
     * @param kos      The Kos object to validate.
     * @param isCreate True if validating for creation (all required fields must be present),
     *                 False if validating for update (only checks validity if fields are provided).
     */
    private void validateKosInput(Kos kos, boolean isCreate) {
        if (kos == null) {
            throw new IllegalArgumentException("Kos data cannot be null.");
        }
        if (isCreate || kos.getName() != null) {
            if (!StringUtils.hasText(kos.getName())) { // Check for null, empty, or whitespace only
                throw new IllegalArgumentException("Kos name cannot be null or empty.");
            }
        }
        if (isCreate || kos.getAddress() != null) {
            if (!StringUtils.hasText(kos.getAddress())) {
                throw new IllegalArgumentException("Kos address cannot be null or empty.");
            }
        }
        if (isCreate || kos.getNumRooms() != null) {
            if (kos.getNumRooms() == null || kos.getNumRooms() <= 0) {
                throw new IllegalArgumentException("Number of rooms must be positive.");
            }
        }
        if (isCreate || kos.getMonthlyRentPrice() != null) {
            if (kos.getMonthlyRentPrice() == null || kos.getMonthlyRentPrice().signum() <= 0) { // signum() checks if > 0
                throw new IllegalArgumentException("Monthly rent price must be positive.");
            }
        }
    }
}