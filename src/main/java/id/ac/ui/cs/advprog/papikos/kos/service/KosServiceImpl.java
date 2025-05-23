package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.kos.exception.KosNotFoundException;
import id.ac.ui.cs.advprog.papikos.kos.exception.UnauthorizedAccessException;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.repository.KosRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
// @RequiredArgsConstructor // We will manually create constructor for RestTemplate injection
public class KosServiceImpl implements KosService {

    private static final Logger logger = LoggerFactory.getLogger(KosServiceImpl.class);

    private final KosRepository kosRepository;

    // Constructor injection
    public KosServiceImpl(KosRepository kosRepository, RestTemplate restTemplate) {
        this.kosRepository = kosRepository;
    }

    @Override
    @Transactional
    public Kos createKos(Kos kos, UUID ownerUserId) {
        validateKosInput(kos, true);
        kos.setOwnerUserId(ownerUserId);
        logger.info("Creating Kos with name '{}' for ownerId {}", kos.getName(), ownerUserId);
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

        validateKosInput(updatedKosData, false);

        if (updatedKosData.getName() != null) {
            existingKos.setName(updatedKosData.getName());
        }
        if (updatedKosData.getAddress() != null) {
            existingKos.setAddress(updatedKosData.getAddress());
        }
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
        logger.info("Updating Kos with ID '{}' by user {}", kosId, requestingUserId);
        return kosRepository.save(existingKos);
    }

    @Override
    @Transactional
    public void deleteKos(UUID kosId, UUID requestingUserId) {
        Kos kosToDelete = findKosById(kosId);

        if (!kosToDelete.getOwnerUserId().equals(requestingUserId)) {
            throw new UnauthorizedAccessException("User " + requestingUserId + " is not authorized to delete Kos " + kosId);
        }
        logger.info("Deleting Kos with ID '{}' by user {}", kosId, requestingUserId);
        kosRepository.deleteById(kosId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Kos> searchKos(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }
        return kosRepository.findKosByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                keyword, keyword, keyword
        );
    }

    private void validateKosInput(Kos kos, boolean isCreate) {
        if (kos == null) {
            throw new IllegalArgumentException("Kos data cannot be null.");
        }
        if (isCreate || kos.getName() != null) {
            if (!StringUtils.hasText(kos.getName())) {
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
            if (kos.getMonthlyRentPrice() == null || kos.getMonthlyRentPrice().signum() <= 0) {
                throw new IllegalArgumentException("Monthly rent price must be positive.");
            }
        }
    }
}