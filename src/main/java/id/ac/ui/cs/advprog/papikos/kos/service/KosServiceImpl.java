package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.kos.exception.InvalidOwnerException;
import id.ac.ui.cs.advprog.papikos.kos.exception.KosNotFoundException;
import id.ac.ui.cs.advprog.papikos.kos.exception.UnauthorizedAccessException;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.repository.KosRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
// @RequiredArgsConstructor // We will manually create constructor for RestTemplate injection
public class KosServiceImpl implements KosService {

    private static final Logger logger = LoggerFactory.getLogger(KosServiceImpl.class);

    private final KosRepository kosRepository;
    private final RestTemplate restTemplate;

    @Value("${owner.service.url}")
    private String ownerServiceBaseUrl; // Will be like "http://localhost:8088"

    // Constructor injection
    public KosServiceImpl(KosRepository kosRepository, RestTemplate restTemplate) {
        this.kosRepository = kosRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Checks if the ownerId is valid by making an HTTP POST request to an external owner service.
     *
     * @param ownerUserId The UUID of the owner to validate.
     * @return true if the owner is valid, false otherwise.
     */
    private boolean isValidOwner(UUID ownerUserId) {
        String ownerValidationUrl = ownerServiceBaseUrl + "/api/owner"; // As per requirement: <mock_url>/api/owner

        // Prepare mock request body: e.g., {"id": "uuid-string-of-owner"}
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("id", ownerUserId.toString()); // Using "id" as a common field name for the mock body

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            logger.info("Validating ownerId {} at URL: {}", ownerUserId, ownerValidationUrl);
            // Expecting a 2xx response for a valid owner.
            // The actual response body content might not matter, just the status code.
            ResponseEntity<String> response = restTemplate.postForEntity(ownerValidationUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("OwnerId {} validation successful. Status: {}", ownerUserId, response.getStatusCode());
                return true;
            } else {
                // This case might not be hit if non-2xx throws an exception, but good for completeness
                logger.warn("OwnerId {} validation failed. Status: {}, Body: {}", ownerUserId, response.getStatusCode(), response.getBody());
                return false;
            }
        } catch (HttpClientErrorException e) {
            // Handles 4xx client errors (e.g., 400 Bad Request, 404 Not Found if owner invalid)
            logger.warn("Client error during ownerId {} validation: {} - {}", ownerUserId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return false;
        } catch (RestClientException e) {
            // Handles other errors like network issues, 5xx server errors from the mock service
            logger.error("Error during ownerId {} validation via external service: {}", ownerUserId, e.getMessage(), e);
            // Depending on policy, you might want to throw a specific exception here
            // For now, consider it as owner not validated.
            return false;
        }
    }

    @Override
    @Transactional
    public Kos createKos(Kos kos, UUID ownerUserId) {
        // Perform ownerId validation
        if (!isValidOwner(ownerUserId)) {
            throw new InvalidOwnerException("Owner ID " + ownerUserId + " is not valid or owner service is unavailable.");
        }

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
        // Optionally, you could also call isValidOwner here if you want to ensure
        // the owner still exists before fetching their Kos, but it might be overkill
        // and add unnecessary external calls. The primary check is on creation.
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