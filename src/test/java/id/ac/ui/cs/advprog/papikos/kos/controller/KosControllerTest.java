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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication; // For providing custom Authentication

@WebMvcTest(KosController.class)
class KosControllerTest {

    private MockMvc mockMvc;

    @Mock
    private KosService kosService;

    @InjectMocks
    private KosController kosController;

    // JacksonTester for serializing request bodies if needed
    private JacksonTester<Kos> jsonKosRequest;
    private JacksonTester<Map<String, Object>> jsonPatchRequest;


    // Test data
    private Kos kos;
    private Kos anotherKos;
    private UUID kosId;
    private UUID ownerUserId;
    private UUID anotherUserId;
    private UUID nonExistentKosId;
    private Authentication ownerAuth; // For simulating Authentication object

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        JacksonTester.initFields(this, objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(kosController).build();

        kosId = UUID.randomUUID();
        ownerUserId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();
        nonExistentKosId = UUID.randomUUID();

        // Simulate Authentication object for the owner
        ownerAuth = new UsernamePasswordAuthenticationToken(
                ownerUserId.toString(), // Principal's name (the UUID string)
                null,
                Collections.singletonList(new SimpleGrantedAuthority("PEMILIK")) // Role/Authority
        );

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

        anotherKos = new Kos();
        anotherKos.setId(UUID.randomUUID());
        anotherKos.setOwnerUserId(anotherUserId);
        anotherKos.setName("Another Owner's Kos");
        anotherKos.setAddress("Jl. Auth Test 99");
    }

    private Authentication createAuth(UUID userId, String role) {
        return new UsernamePasswordAuthenticationToken(
                userId.toString(), null, Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    // == POST /api/v1/kos ==
    @Test
    // @WithMockUser is simpler if role check is enough, but using custom auth for clarity on UUID passing
    // @WithMockUser(username = "owner-uuid-string", authorities = {"PEMILIK"})
    void postKos_Success_Returns201() throws Exception {
        Kos kosToCreate = new Kos();
        kosToCreate.setName(kos.getName());
        kosToCreate.setAddress(kos.getAddress());
        kosToCreate.setDescription(kos.getDescription());
        kosToCreate.setNumRooms(kos.getNumRooms());
        kosToCreate.setMonthlyRentPrice(kos.getMonthlyRentPrice());

        // Mock service call - expect ownerUserId from principal
        when(kosService.createKos(any(Kos.class), eq(ownerUserId))).thenReturn(kos); // Return the full kos

        mockMvc.perform(post("/api/v1/kos")
                        .with(csrf())
                        .with(authentication(ownerAuth)) // Provide the Authentication object
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonKosRequest.write(kosToCreate).getJson()))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value())) // Check integer status
                .andExpect(jsonPath("$.message").value("Kos created successfully")) // Expected message
                .andExpect(jsonPath("$.data.id").value(kosId.toString()))
                .andExpect(jsonPath("$.data.ownerUserId").value(ownerUserId.toString()))
                .andExpect(jsonPath("$.data.name").value(kos.getName()))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(kosService, times(1)).createKos(any(Kos.class), eq(ownerUserId));
    }

    @Test
    @WithMockUser(username = "tenant-user", authorities = {"PENYEWA"})
        // Role is wrong
    void postKos_UnauthorizedRole_Returns403() throws Exception {
        Kos kosToCreate = new Kos();
        kosToCreate.setName("Attempt by Tenant");

        mockMvc.perform(post("/api/v1/kos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonKosRequest.write(kosToCreate).getJson()))
                .andExpect(status().isForbidden()); // Spring Security should handle this

        verify(kosService, never()).createKos(any(Kos.class), any(UUID.class));
    }

    // Test for @Valid failure (assuming @ControllerAdvice handles MethodArgumentNotValidException)
    @Test
    void postKos_InvalidData_Returns400() throws Exception {
        Kos invalidKos = new Kos(); // Missing required fields like name
        invalidKos.setAddress("Jl. Invalid");

        // Mocking the service is NOT needed here, validation happens before service call

        mockMvc.perform(post("/api/v1/kos")
                        .with(csrf())
                        .with(authentication(ownerAuth)) // Need auth to pass security filter
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonKosRequest.write(invalidKos).getJson()))
                .andExpect(status().isBadRequest()) // Expect 400 due to @Valid failure
                // Optionally check response body if @ControllerAdvice provides one for validation errors
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").isNotEmpty()); // Message will depend on validation failure details


        verify(kosService, never()).createKos(any(Kos.class), any(UUID.class));
    }


    // == GET /api/v1/kos ==
    @Test
    void getAllKos_Success_Returns200() throws Exception {
        List<Kos> allKosList = List.of(kos, anotherKos);
        when(kosService.findAllKos()).thenReturn(allKosList);

        mockMvc.perform(get("/api/v1/kos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Kos list fetched successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(kosId.toString()))
                .andExpect(jsonPath("$.data[1].ownerUserId").value(anotherUserId.toString()));

        verify(kosService, times(1)).findAllKos();
        verify(kosService, never()).searchKos(anyString()); // Ensure search wasn't called
    }

    @Test
    void searchKos_Success_Returns200() throws Exception {
        String keyword = "Controller Test";
        List<Kos> searchResult = List.of(kos);
        when(kosService.searchKos(eq(keyword))).thenReturn(searchResult);

        mockMvc.perform(get("/api/v1/kos")
                        .param("keyword", keyword)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Kos search results fetched successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(kosId.toString()));

        verify(kosService, times(1)).searchKos(eq(keyword));
        verify(kosService, never()).findAllKos(); // Ensure findAll wasn't called
    }

    // == GET /api/v1/kos/my ==
    @Test
    void getMyKos_Owner_Success_Returns200() throws Exception {
        List<Kos> myKosList = List.of(kos);
        when(kosService.findKosByOwnerUserId(eq(ownerUserId))).thenReturn(myKosList);

        mockMvc.perform(get("/api/v1/kos/my")
                        .with(authentication(ownerAuth)) // Provide owner's auth
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Owner's Kos list fetched successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(kosId.toString()))
                .andExpect(jsonPath("$.data[0].ownerUserId").value(ownerUserId.toString()));

        verify(kosService, times(1)).findKosByOwnerUserId(eq(ownerUserId));
    }

    @Test
    @WithMockUser(username = "tenant-user", authorities = {"PENYEWA"})
    void getMyKos_Tenant_Forbidden_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/kos/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(kosService, never()).findKosByOwnerUserId(any(UUID.class));
    }

    // == GET /api/v1/kos/{id} ==
    @Test
    void getKosById_Success_Returns200() throws Exception {
        when(kosService.findKosById(eq(kosId))).thenReturn(kos);

        mockMvc.perform(get("/api/v1/kos/{id}", kosId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Kos details fetched successfully"))
                .andExpect(jsonPath("$.data.id").value(kosId.toString()))
                .andExpect(jsonPath("$.data.name").value(kos.getName()));

        verify(kosService, times(1)).findKosById(eq(kosId));
    }

    // Test for service throwing exception (assuming @ControllerAdvice handles it)
    @Test
    void getKosById_NotFound_Returns404() throws Exception {
        when(kosService.findKosById(eq(nonExistentKosId))).thenThrow(new KosNotFoundException(nonExistentKosId));

        mockMvc.perform(get("/api/v1/kos/{id}", nonExistentKosId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                // Check response body if @ControllerAdvice provides one
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("Kos with ID " + nonExistentKosId + " not found"));

        verify(kosService, times(1)).findKosById(eq(nonExistentKosId));
    }

    // == PATCH /api/v1/kos/{id} ==
    @Test
    void patchKos_Owner_Success_Returns200() throws Exception {
        String updatedName = "Updated Kos Name via Patch";
        int updatedRooms = 6;
        Map<String, Object> updatePayload = Map.of("name", updatedName, "numRooms", updatedRooms);

        Kos expectedResultKos = new Kos(); // Create the expected state after update
        // Copy necessary fields...
        expectedResultKos.setId(kosId);
        expectedResultKos.setOwnerUserId(ownerUserId);
        expectedResultKos.setName(updatedName);
        expectedResultKos.setNumRooms(updatedRooms);
        expectedResultKos.setAddress(kos.getAddress()); // Assume address not updated
        //... set other fields as expected after update

        // Mock service updateKos
        when(kosService.updateKos(eq(kosId), any(Kos.class), eq(ownerUserId))).thenReturn(expectedResultKos);

        mockMvc.perform(patch("/api/v1/kos/{id}", kosId)
                        .with(csrf())
                        .with(authentication(ownerAuth)) // Provide owner's auth
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPatchRequest.write(updatePayload).getJson()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Kos updated successfully"))
                .andExpect(jsonPath("$.data.id").value(kosId.toString()))
                .andExpect(jsonPath("$.data.name").value(updatedName))
                .andExpect(jsonPath("$.data.numRooms").value(updatedRooms));

        verify(kosService, times(1)).updateKos(eq(kosId), any(Kos.class), eq(ownerUserId));
    }

    // Test PATCH not found (assuming @ControllerAdvice handles KosNotFoundException)
    @Test
    void patchKos_Owner_NotFound_Returns404() throws Exception {
        Map<String, Object> updatePayload = Map.of("name", "Attempt Update NonExistent");

        when(kosService.updateKos(eq(nonExistentKosId), any(Kos.class), eq(ownerUserId)))
                .thenThrow(new KosNotFoundException(nonExistentKosId));

        mockMvc.perform(patch("/api/v1/kos/{id}", nonExistentKosId)
                        .with(csrf())
                        .with(authentication(ownerAuth)) // Provide owner's auth
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPatchRequest.write(updatePayload).getJson()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("Kos with ID " + nonExistentKosId + " not found"));

        verify(kosService, times(1)).updateKos(eq(nonExistentKosId), any(Kos.class), eq(ownerUserId));
    }

    @Test
    @WithMockUser(username = "tenant-user", authorities = {"PENYEWA"})
    void patchKos_Tenant_Forbidden_Returns403() throws Exception {
        Map<String, Object> updatePayload = Map.of("name", "Tenant Attempt Update");

        mockMvc.perform(patch("/api/v1/kos/{id}", kosId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPatchRequest.write(updatePayload).getJson()))
                .andExpect(status().isForbidden());

        verify(kosService, never()).updateKos(any(UUID.class), any(Kos.class), any(UUID.class));
    }

    // Test PATCH unauthorized (assuming @ControllerAdvice handles UnauthorizedAccessException)
    @Test
    void patchKos_OwnerUpdatesOthersKos_Forbidden_Returns403() throws Exception {
        Map<String, Object> updatePayload = Map.of("name", "Attempt Update Other's Kos");
        Authentication attackerAuth = createAuth(anotherUserId, "PEMILIK"); // Auth for the attacker
        String expectedErrorMessage = "User " + anotherUserId + " is not authorized to update Kos " + kosId;

        // Mock service to throw UnauthorizedAccessException
        when(kosService.updateKos(eq(kosId), any(Kos.class), eq(anotherUserId)))
                .thenThrow(new UnauthorizedAccessException(expectedErrorMessage));

        mockMvc.perform(patch("/api/v1/kos/{id}", kosId) // Target the original kos
                        .with(csrf())
                        .with(authentication(attackerAuth)) // Use attacker's auth
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPatchRequest.write(updatePayload).getJson()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                .andExpect(jsonPath("$.message").value(expectedErrorMessage));

        verify(kosService, times(1)).updateKos(eq(kosId), any(Kos.class), eq(anotherUserId));
    }


    // == DELETE /api/v1/kos/{id} ==
    @Test
    void deleteKos_Owner_Success_Returns204() throws Exception {
        // Mock service deleteKos
        doNothing().when(kosService).deleteKos(eq(kosId), eq(ownerUserId));

        mockMvc.perform(delete("/api/v1/kos/{id}", kosId)
                        .with(csrf())
                        .with(authentication(ownerAuth))) // Provide owner's auth
                .andExpect(status().isNoContent()); // Expect 204 No Content

        verify(kosService, times(1)).deleteKos(eq(kosId), eq(ownerUserId));
    }

    // Test DELETE not found (assuming @ControllerAdvice handles KosNotFoundException)
    @Test
    void deleteKos_Owner_NotFound_Returns404() throws Exception {
        // Mock service deleteKos to throw exception
        doThrow(new KosNotFoundException(nonExistentKosId)).when(kosService).deleteKos(eq(nonExistentKosId), eq(ownerUserId));

        mockMvc.perform(delete("/api/v1/kos/{id}", nonExistentKosId)
                        .with(csrf())
                        .with(authentication(ownerAuth))) // Provide owner's auth
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("Kos with ID " + nonExistentKosId + " not found"));


        verify(kosService, times(1)).deleteKos(eq(nonExistentKosId), eq(ownerUserId));
    }

    @Test
    @WithMockUser(username = "tenant-user", authorities = {"PENYEWA"})
    void deleteKos_Tenant_Forbidden_Returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/kos/{id}", kosId)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(kosService, never()).deleteKos(any(UUID.class), any(UUID.class));
    }

    // Test DELETE unauthorized (assuming @ControllerAdvice handles UnauthorizedAccessException)
    @Test
    void deleteKos_OwnerDeletesOthersKos_Forbidden_Returns403() throws Exception {
        Authentication attackerAuth = createAuth(anotherUserId, "PEMILIK");
        String expectedErrorMessage = "User " + anotherUserId + " is not authorized to delete Kos " + kosId;

        // Mock service deleteKos to throw exception
        doThrow(new UnauthorizedAccessException(expectedErrorMessage))
                .when(kosService).deleteKos(eq(kosId), eq(anotherUserId));

        mockMvc.perform(delete("/api/v1/kos/{id}", kosId) // Target the original kos
                        .with(csrf())
                        .with(authentication(attackerAuth))) // Use attacker's auth
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                .andExpect(jsonPath("$.message").value(expectedErrorMessage));

        verify(kosService, times(1)).deleteKos(eq(kosId), eq(anotherUserId));
    }
}
