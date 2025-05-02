package id.ac.ui.cs.advprog.papikos.kos.controller;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.response.ApiResponse;
import id.ac.ui.cs.advprog.papikos.kos.service.KosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/kos")
@RequiredArgsConstructor
public class KosController {

    private final KosService kosService;

    /**
     * Helper method to extract UUID from Authentication principal name.
     * Throws IllegalArgumentException if parsing fails.
     */
    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authentication principal is required but missing.");
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing UUID from principal name: " + authentication.getName());
            throw new IllegalArgumentException("Invalid user identifier format in authentication token.");
        }
    }

    // --- CREATE ---
    @PostMapping
    @PreAuthorize("hasAuthority('PEMILIK')")
    public ResponseEntity<ApiResponse<Kos>> createKos(@RequestBody Kos kos, Authentication authentication) {
        UUID ownerUserId = getUserIdFromAuthentication(authentication);
        Kos createdKos = kosService.createKos(kos, ownerUserId);
        ApiResponse<Kos> response = ApiResponse.<Kos>builder()
                .status(HttpStatus.CREATED)
                .message("Kos created successfully")
                .data(createdKos)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- READ ---
    @GetMapping
    public ResponseEntity<ApiResponse<List<Kos>>> getAllOrSearchKos(
            @RequestParam(value = "keyword", required = false) String keyword) {

        List<Kos> kosList;
        String message;

        if (keyword != null && !keyword.trim().isEmpty()) {
            kosList = kosService.searchKos(keyword);
            message = "Kos search results fetched successfully";
        } else {
            kosList = kosService.findAllKos();
            message = "Kos list fetched successfully";
        }

        ApiResponse<List<Kos>> response = ApiResponse.<List<Kos>>builder()
                .status(HttpStatus.OK)
                .message(message)
                .data(kosList)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('PEMILIK')")
    public ResponseEntity<ApiResponse<List<Kos>>> getMyKos(Authentication authentication) {
        UUID ownerUserId = getUserIdFromAuthentication(authentication);
        List<Kos> myKosList = kosService.findKosByOwnerUserId(ownerUserId);
        ApiResponse<List<Kos>> response = ApiResponse.<List<Kos>>builder()
                .status(HttpStatus.OK)
                .message("Owner's Kos list fetched successfully")
                .data(myKosList)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Kos>> getKosById(@PathVariable("id") UUID kosId) {
        Kos kos = kosService.findKosById(kosId);
        ApiResponse<Kos> response = ApiResponse.<Kos>builder()
                .status(HttpStatus.OK)
                .message("Kos details fetched successfully")
                .data(kos)
                .build();
        return ResponseEntity.ok(response);
    }

    // --- UPDATE ---
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('PEMILIK')")
    public ResponseEntity<ApiResponse<Kos>> updateKos(@PathVariable("id") UUID kosId,
                                                      @RequestBody Kos kosUpdateData, // Use full object, service handles partial update
                                                      Authentication authentication) {
        UUID requestingUserId = getUserIdFromAuthentication(authentication);
        Kos updatedKos = kosService.updateKos(kosId, kosUpdateData, requestingUserId);
        ApiResponse<Kos> response = ApiResponse.<Kos>builder()
                .status(HttpStatus.OK)
                .message("Kos updated successfully")
                .data(updatedKos)
                .build();
        return ResponseEntity.ok(response);
    }


    // --- DELETE ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PEMILIK')")
    public ResponseEntity<Void> deleteKos(@PathVariable("id") UUID kosId, Authentication authentication) {
        UUID requestingUserId = getUserIdFromAuthentication(authentication);
        kosService.deleteKos(kosId, requestingUserId);
        return ResponseEntity.noContent().build();
    }
}
