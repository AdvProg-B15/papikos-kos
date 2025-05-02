package id.ac.ui.cs.advprog.papikos.kos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.service.KosService;
import id.ac.ui.cs.advprog.papikos.kos.exception.KosNotFoundException;
import id.ac.ui.cs.advprog.papikos.kos.exception.UnauthorizedAccessException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(KosController.class) // Target only the KosController
class KosControllerTest {

    private MockMvc mockMvc;

    @Mock // Create a Mockito mock for the service
    private KosService kosService;

    @InjectMocks // Create an instance of the controller and inject @Mock fields into it
    private KosController kosController;

    // JacksonTester for easy JSON conversion
    private JacksonTester<Kos> jsonKos;
    private JacksonTester<List<Kos>> jsonKosList;
    private JacksonTester<Object> jsonObject; // For testing request bodies without full Kos mapping

    // Test data using UUIDs
    private Kos kos;
    private Kos anotherKos;
    private UUID kosId;
    private UUID ownerUserId;
    private UUID anotherUserId;
    private UUID nonExistentKosId;

    @BeforeEach
    void setUp() {
        // Configure JacksonTester with ObjectMapper that supports Java 8 time
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        JacksonTester.initFields(this, objectMapper);

        // Setup MockMvc for standalone controller testing
        mockMvc = MockMvcBuilders.standaloneSetup(kosController).build();

        // Initialize UUIDs
        kosId = UUID.randomUUID();
        ownerUserId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID(); // For simulating a different owner
        nonExistentKosId = UUID.randomUUID();

        // Initialize main test Kos owned by ownerUserId
        kos = new Kos();
        kos.setId(kosId);
        kos.setOwnerUserId(ownerUserId);
        kos.setName("Test Kos Controller");
        kos.setAddress("Jl. Controller Test 1");
        kos.setDescription("Kos for controller testing");
        kos.setNumRooms(5);
        kos.setMonthlyRentPrice(new BigDecimal("1200000.00"));
        kos.setIsListed(true);
        kos.setCreatedAt(LocalDateTime.now().minusDays(2));
        kos.setUpdatedAt(LocalDateTime.now().minusDays(1));

        // Initialize another Kos owned by anotherUserId (for authorization tests)
        anotherKos = new Kos();
        anotherKos.setId(UUID.randomUUID()); // Give it a unique ID
        anotherKos.setOwnerUserId(anotherUserId);
        anotherKos.setName("Another Owner's Kos");
        anotherKos.setAddress("Jl. Auth Test 99");
        // Set other properties as needed...
    }

    // == POST /api/v1/kos == (Adjust path based on your @RequestMapping)
    @Test
    @WithMockUser(username = "user-who-posts", // Username doesn't have to be UUID here, depends on how controller gets ID
            authorities = {"PEMILIK"})
    // Role/Authority check
    void postKos_Success_Returns201() throws Exception {
        // Arrange: Prepare the Kos to be sent (ID is usually null before creation)
        Kos kosToCreate = new Kos();
        kosToCreate.setName(kos.getName());
        kosToCreate.setAddress(kos.getAddress());
        kosToCreate.setDescription(kos.getDescription());
        kosToCreate.setNumRooms(kos.getNumRooms());
        kosToCreate.setMonthlyRentPrice(kos.getMonthlyRentPrice());
        // ownerUserId will be set by the controller/service based on authenticated user

        // Arrange: Mock the service call
        // Assuming controller extracts user ID (ownerUserId) from Principal/Authentication
        // and passes it to the service.
        // We pass ownerUserId here to simulate the ID controller would extract.
        when(kosService.createKos(any(Kos.class), eq(ownerUserId))).thenReturn(kos); // Return the fully formed kos

        // Simulate how controller gets the ownerUserId (replace with your actual mechanism if different)
        // For the test, we assume @WithMockUser's username *could* be the UUID string, or
        // the controller uses some Authentication principal details.
        // Let's test as if the controller can resolve "user-who-posts" to ownerUserId.
        // A more direct test might use ownerUserId.toString() in @WithMockUser if that's how it works.

        // Act & Assert
        mockMvc.perform(post("/api/v1/kos") // Adjust path if needed
                        .with(csrf()) // Include CSRF token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonKos.write(kosToCreate).getJson())
                        .principal(() -> ownerUserId.toString())) // Mock principal if needed, or rely on @WithMockUser
                .andExpect(status().isCreated()) // Assert status 201 Created
                // Check response body (adjust jsonPath based on your actual ApiResponse structure)
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.message").value("Kos created successfully")) // Adjust message
                .andExpect(jsonPath("$.data.id").value(kosId.toString()))
                .andExpect(jsonPath("$.data.ownerUserId").value(ownerUserId.toString()))
                .andExpect(jsonPath("$.data.name").value(kos.getName()));

        // Verify service method was called correctly
        verify(kosService, times(1)).createKos(any(Kos.class), eq(ownerUserId));
    }

    @Test
    @WithMockUser(username = "tenant-user", authorities = {"PENYEWA"})
        // User with wrong role
    void postKos_UnauthorizedRole_Returns403() throws Exception {
        // Arrange: Prepare some Kos data (content doesn't strictly matter for auth failure)
        Kos kosToCreate = new Kos();
        kosToCreate.setName("Attempt by Tenant");
        // ... set other fields

        // Act & Assert
        mockMvc.perform(post("/api/v1/kos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonKos.write(kosToCreate).getJson()))
                .andExpect(status().isForbidden()); // Assert status 403 Forbidden

        // Verify service method was NOT called
        verify(kosService, never()).createKos(any(Kos.class), any(UUID.class));
    }

    @Test
    @WithMockUser(username = "owner-user", authorities = {"PEMILIK"})
    void postKos_InvalidData_Returns400() throws Exception {
        // Arrange: Prepare invalid Kos JSON (e.g., missing required field 'name')
        Kos invalidKos = new Kos();
        // invalidKos.setName(null); // Assume name is required
        invalidKos.setAddress("Jl. Invalid");
        invalidKos.setNumRooms(1);
        invalidKos.setMonthlyRentPrice(BigDecimal.TEN);

        // Act & Assert
        mockMvc.perform(post("/api/v1/kos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonKos.write(invalidKos).getJson())
                        .principal(() -> ownerUserId.toString())) // Provide principal if needed by controller logic before validation
                .andExpect(status().isBadRequest()); // Assert status 400 Bad Request (due to validation)

        // Verify service method was NOT called due to validation failure
        verify(kosService, never()).createKos(any(Kos.class), any(UUID.class));
    }

    // == GET /api/v1/kos ==
    @Test
    // @WithMockUser // No specific user needed if public access
    void getAllKos_Success_Returns200() throws Exception {
        // Arrange
        List<Kos> allKosList = List.of(kos, anotherKos);
        when(kosService.findAllKos()).thenReturn(allKosList);

        // Act & Assert
        mockMvc.perform(get("/api/v1/kos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Assert status 200 OK
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Kos list fetched successfully")) // Adjust message
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(kosId.toString()))
                .andExpect(jsonPath("$.data[1].ownerUserId").value(anotherUserId.toString()));

        // Verify
        verify(kosService, times(1)).findAllKos();
    }

    @Test
    void searchKos_Success_Returns200() throws Exception {
        // Arrange
        String keyword = "Controller Test";
        List<Kos> searchResult = List.of(kos); // Assume 'kos' matches the keyword
        // Assuming a simple search service method for this example
        when(kosService.searchKos(eq(keyword))).thenReturn(searchResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/kos")
                        .param("keyword", keyword) // Add search parameter
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Kos search results fetched successfully")) // Adjust message
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(kosId.toString()))
                .andExpect(jsonPath("$.data[0].name").value(kos.getName()));

        // Verify
        verify(kosService, times(1)).searchKos(eq(keyword));
        verify(kosService, never()).findAllKos(); // Ensure findAll wasn't called
    }

    // == GET /api/v1/kos/my ==
    @Test
    @WithMockUser(username = "user-for-mykos", authorities = {"PEMILIK"})
    void getMyKos_Owner_Success_Returns200() throws Exception {
        // Arrange
        List<Kos> myKosList = List.of(kos);
        // Assume controller gets ownerUserId from principal and calls service
        when(kosService.findKosByOwnerUserId(eq(ownerUserId))).thenReturn(myKosList);

        // Act & Assert
        mockMvc.perform(get("/api/v1/kos/my")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> ownerUserId.toString())) // Provide principal
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Owner's Kos list fetched successfully")) // Adjust message
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(kosId.toString()))
                .andExpect(jsonPath("$.data[0].ownerUserId").value(ownerUserId.toString()));

        // Verify
        verify(kosService, times(1)).findKosByOwnerUserId(eq(ownerUserId));
    }

    @Test
    @WithMockUser(username = "tenant-user", authorities = {"PENYEWA"})
    void getMyKos_Tenant_Forbidden_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/kos/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // Assert status 403 Forbidden

        // Verify
        verify(kosService, never()).findKosByOwnerUserId(any(UUID.class));
    }

    // == GET /api/v1/kos/{id} ==
    @Test
    // @WithMockUser // Public access assumed
    void getKosById_Success_Returns200() throws Exception {
        // Arrange
        when(kosService.findKosById(eq(kosId))).thenReturn(kos);

        // Act & Assert
        mockMvc.perform(get("/api/v1/kos/{id}", kosId) // Pass UUID as path variable
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Kos details fetched successfully")) // Adjust message
                .andExpect(jsonPath("$.data.id").value(kosId.toString()))
                .andExpect(jsonPath("$.data.name").value(kos.getName()))
                .andExpect(jsonPath("$.data.ownerUserId").value(ownerUserId.toString()));

        // Verify
        verify(kosService, times(1)).findKosById(eq(kosId));
    }

    @Test
    void getKosById_NotFound_Returns404() throws Exception {
        // Arrange: Mock service to throw exception for the non-existent ID
        when(kosService.findKosById(eq(nonExistentKosId))).thenThrow(new KosNotFoundException(nonExistentKosId));

        // Act & Assert
        mockMvc.perform(get("/api/v1/kos/{id}", nonExistentKosId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Assert status 404 Not Found

        // Verify
        verify(kosService, times(1)).findKosById(eq(nonExistentKosId));
    }

    // == PATCH /api/v1/kos/{id} ==
    @Test
    @WithMockUser(username = "owner-for-patch", authorities = {"PEMILIK"})
    void patchKos_Owner_Success_Returns200() throws Exception {
        // Arrange: Prepare update data
        String updatedName = "Updated Kos Name via Patch";
        Kos updatePayload = new Kos(); // Payload might only contain fields to update
        updatePayload.setName(updatedName);
        updatePayload.setNumRooms(6); // Example of updating another field

        // Arrange: Prepare the state *after* the update for mocking service return
        Kos expectedResultKos = new Kos();
        expectedResultKos.setId(kosId);
        expectedResultKos.setOwnerUserId(ownerUserId); // Owner shouldn't change
        expectedResultKos.setName(updatedName);
        expectedResultKos.setAddress(kos.getAddress()); // Unchanged field
        expectedResultKos.setDescription(kos.getDescription()); // Unchanged field
        expectedResultKos.setNumRooms(6); // Changed field
        expectedResultKos.setMonthlyRentPrice(kos.getMonthlyRentPrice()); // Unchanged field
        expectedResultKos.setIsListed(kos.getIsListed()); // Unchanged field
        expectedResultKos.setUpdatedAt(LocalDateTime.now()); // Should be updated


        // Mock service updateKos(kosId, updatedData, ownerUserId)
        // Assuming controller extracts ownerUserId from principal
        when(kosService.updateKos(eq(kosId), any(Kos.class), eq(ownerUserId))).thenReturn(expectedResultKos);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/kos/{id}", kosId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.write(Map.of("name", updatedName, "numRooms", 6)).getJson()) // Send partial update
                        .principal(() -> ownerUserId.toString())) // Provide principal
                .andExpect(status().isOk()) // Assert status 200 OK
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Kos updated successfully")) // Adjust message
                .andExpect(jsonPath("$.data.id").value(kosId.toString()))
                .andExpect(jsonPath("$.data.name").value(updatedName))
                .andExpect(jsonPath("$.data.numRooms").value(6));

        // Verify
        verify(kosService, times(1)).updateKos(eq(kosId), any(Kos.class), eq(ownerUserId));
    }


    @Test
    @WithMockUser(username = "owner-for-patch-fail", authorities = {"PEMILIK"})
    void patchKos_Owner_NotFound_Returns404() throws Exception {
        // Arrange: Prepare update data
        String updatedName = "Attempt Update NonExistent";
        Kos updatePayload = new Kos();
        updatePayload.setName(updatedName);

        // Mock service updateKos to throw KosNotFoundException
        // Assuming controller extracts ownerUserId from principal
        when(kosService.updateKos(eq(nonExistentKosId), any(Kos.class), eq(ownerUserId)))
                .thenThrow(new KosNotFoundException(nonExistentKosId));

        // Act & Assert
        mockMvc.perform(patch("/api/v1/kos/{id}", nonExistentKosId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonKos.write(updatePayload).getJson())
                        .principal(() -> ownerUserId.toString())) // Provide principal
                .andExpect(status().isNotFound()); // Assert status 404 Not Found

        // Verify
        verify(kosService, times(1)).updateKos(eq(nonExistentKosId), any(Kos.class), eq(ownerUserId));
    }

    @Test
    @WithMockUser(username = "tenant-user", authorities = {"PENYEWA"})
    void patchKos_Tenant_Forbidden_Returns403() throws Exception {
        // Arrange: Prepare update data (content doesn't matter)
        Kos updatePayload = new Kos();
        updatePayload.setName("Tenant Attempt Update");

        // Act & Assert
        mockMvc.perform(patch("/api/v1/kos/{id}", kosId) // Target existing kos
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonKos.write(updatePayload).getJson()))
                .andExpect(status().isForbidden()); // Assert status 403 Forbidden

        // Verify service was NOT called
        verify(kosService, never()).updateKos(any(UUID.class), any(Kos.class), any(UUID.class));
    }

    @Test
    @WithMockUser(username = "attacker-owner", authorities = {"PEMILIK"})
        // Authenticated as a *different* owner
    void patchKos_OwnerUpdatesOthersKos_Forbidden_Returns403() throws Exception {
        // Arrange: Prepare update data
        Kos updatePayload = new Kos();
        updatePayload.setName("Attempt Update Other's Kos");
        UUID attackerOwnerId = UUID.randomUUID(); // The ID of the logged-in attacker

        // Mock service updateKos to throw UnauthorizedAccessException when attackerOwnerId tries to update kosId (owned by ownerUserId)
        when(kosService.updateKos(eq(kosId), any(Kos.class), eq(attackerOwnerId)))
                .thenThrow(new UnauthorizedAccessException("User " + attackerOwnerId + " cannot update Kos " + kosId));

        // Act & Assert
        mockMvc.perform(patch("/api/v1/kos/{id}", kosId) // Target the original kos
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonKos.write(updatePayload).getJson())
                        .principal(() -> attackerOwnerId.toString())) // Principal is the attacker
                .andExpect(status().isForbidden()); // Assert status 403 Forbidden

        // Verify service method was called (and threw the exception)
        verify(kosService, times(1)).updateKos(eq(kosId), any(Kos.class), eq(attackerOwnerId));
    }


    // == DELETE /api/v1/kos/{id} ==
    @Test
    @WithMockUser(username = "owner-for-delete", authorities = {"PEMILIK"})
    void deleteKos_Owner_Success_Returns204() throws Exception {
        // Arrange: Mock service deleteKos(kosId, ownerUserId) - void method
        // Assuming controller extracts ownerUserId from principal
        doNothing().when(kosService).deleteKos(eq(kosId), eq(ownerUserId));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/kos/{id}", kosId)
                        .with(csrf())
                        .principal(() -> ownerUserId.toString())) // Provide principal
                .andExpect(status().isNoContent()); // Assert status 204 No Content

        // Verify
        verify(kosService, times(1)).deleteKos(eq(kosId), eq(ownerUserId));
    }

    @Test
    @WithMockUser(username = "owner-for-delete-fail", authorities = {"PEMILIK"})
    void deleteKos_Owner_NotFound_Returns404() throws Exception {
        // Arrange: Mock service deleteKos to throw KosNotFoundException
        // Assuming controller extracts ownerUserId from principal
        doThrow(new KosNotFoundException(nonExistentKosId)).when(kosService).deleteKos(eq(nonExistentKosId), eq(ownerUserId));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/kos/{id}", nonExistentKosId)
                        .with(csrf())
                        .principal(() -> ownerUserId.toString())) // Provide principal
                .andExpect(status().isNotFound()); // Assert status 404 Not Found

        // Verify
        verify(kosService, times(1)).deleteKos(eq(nonExistentKosId), eq(ownerUserId));
    }

    @Test
    @WithMockUser(username = "tenant-user", authorities = {"PENYEWA"})
    void deleteKos_Tenant_Forbidden_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/kos/{id}", kosId) // Target existing kos
                        .with(csrf()))
                .andExpect(status().isForbidden()); // Assert status 403 Forbidden

        // Verify service was NOT called
        verify(kosService, never()).deleteKos(any(UUID.class), any(UUID.class));
    }

    @Test
    @WithMockUser(username = "attacker-owner-delete", authorities = {"PEMILIK"})
        // Authenticated as a *different* owner
    void deleteKos_OwnerDeletesOthersKos_Forbidden_Returns403() throws Exception {
        // Arrange
        UUID attackerOwnerId = UUID.randomUUID();

        // Mock service deleteKos to throw UnauthorizedAccessException
        doThrow(new UnauthorizedAccessException("User " + attackerOwnerId + " cannot delete Kos " + kosId))
                .when(kosService).deleteKos(eq(kosId), eq(attackerOwnerId));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/kos/{id}", kosId) // Target the original kos
                        .with(csrf())
                        .principal(attackerOwnerId::toString)) // Principal is the attacker
                .andExpect(status().isForbidden()); // Assert status 403 Forbidden

        // Verify service method was called (and threw the exception)
        verify(kosService, times(1)).deleteKos(eq(kosId), eq(attackerOwnerId));
    }
}
