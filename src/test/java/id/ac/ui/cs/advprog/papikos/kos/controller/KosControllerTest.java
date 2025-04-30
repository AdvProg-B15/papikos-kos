package id.ac.ui.cs.advprog.papikos.kos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.Pemilik; // Assuming Pemilik is needed for setup
import id.ac.ui.cs.advprog.papikos.kos.service.KosService;
// Import exceptions if needed for mocking service behavior
import id.ac.ui.cs.advprog.papikos.kos.exception.KosNotFoundException;
import id.ac.ui.cs.advprog.papikos.kos.exception.UnauthorizedAccessException;
import jakarta.validation.ValidationException; // Or specific validation exception

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.json.JacksonTester;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // For POST/PATCH/DELETE with CSRF

@WebMvcTest(KosController.class)
class KosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KosService kosService;

    // JacksonTester for converting objects to/from JSON
    private JacksonTester<Kos> jsonKos;
    private JacksonTester<List<Kos>> jsonKosList;

    private Kos kos;
    private Pemilik owner;
    private String kosId = "kos-123";
    private String ownerId = "owner-456"; // Assuming owner ID is used/available
    private String nonExistentId = "kos-not-found";

    // Add fields for the other owner and their Kos
    private Pemilik anotherOwner;
    private Kos anotherKos;
    private String anotherOwnerId = "owner-789";
    private String anotherKosId = "kos-xyz";

    @BeforeEach
    void setUp() {
        JacksonTester.initFields(this, new ObjectMapper());

        // Initialize common test objects
        owner = new Pemilik();
        owner.setId(ownerId);
        owner.setApproved(true); // Assuming approval status relevant

        kos = new Kos();
        kos.setId(kosId);
        kos.setPemilik(owner);
        kos.setNama("Test Kos Controller");
        // Set other necessary properties

        // Initialize another owner and kos
        anotherOwner = new Pemilik();
        anotherOwner.setId(anotherOwnerId);
        anotherOwner.setApproved(true);

        anotherKos = new Kos();
        anotherKos.setId(anotherKosId);
        anotherKos.setPemilik(anotherOwner); // Link to the other owner
        anotherKos.setNama("Another Owner's Kos");
    }

    // == POST /kos ==
    @Test
    @WithMockUser(username = ownerId, roles = {"PEMILIK"}) // Assuming PEMILIK role for owner
    void postKos_Success_Returns201() throws Exception {
        // Mock service addKos behavior
        // Assumes addKos takes Kos object and ownerId (principal name) and returns the created Kos
        when(kosService.addKos(any(Kos.class), eq(ownerId))).thenReturn(kos);

        // Perform POST request with valid Kos JSON and CSRF token
        mockMvc.perform(post("/kos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonKos.write(kos).getJson()) // Send the kos object as JSON
                        .with(csrf())) // Include CSRF token for POST
                .andExpect(status().isCreated()) // Assert status 201 Created
                // Check response body (assuming ApiResponse structure with data field)
                // Adjust status/message based on actual controller implementation
                .andExpect(jsonPath("$.status").value("CREATED")) 
                .andExpect(jsonPath("$.message").value("Kos added successfully")) 
                .andExpect(jsonPath("$.data.id").value(kosId))
                .andExpect(jsonPath("$.data.nama").value("Test Kos Controller"));

        // Verify that the service method was called
        verify(kosService, times(1)).addKos(any(Kos.class), eq(ownerId));
    }

    @Test
    @WithMockUser(username = "tenant-user", roles = {"PENYEWA"}) // Assuming PENYEWA role for tenant
    void postKos_UnauthorizedTenant_Returns403() throws Exception {
        // Perform POST request (no service mock needed as security should block)
        mockMvc.perform(post("/kos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonKos.write(kos).getJson()) // Content doesn't really matter here
                        .with(csrf()))
                .andExpect(status().isForbidden()); // Assert status 403 Forbidden
    }

    @Test
    @WithMockUser(username = ownerId, roles = {"PEMILIK"})
    void postKos_InvalidData_Returns400() throws Exception {
        // Prepare invalid Kos JSON (e.g., missing required field 'nama')
        Kos invalidKos = new Kos();
        invalidKos.setId(kosId); // Set other fields if needed, but leave 'nama' invalid
        invalidKos.setPemilik(owner);
        // invalidKos.setNama(null); // Or empty string, depending on validation rule

        // Perform POST request with invalid data
        mockMvc.perform(post("/kos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonKos.write(invalidKos).getJson())
                        .with(csrf()))
                .andExpect(status().isBadRequest()); // Assert status 400 Bad Request

        // Verify service method was NOT called due to validation failure
        verify(kosService, never()).addKos(any(Kos.class), anyString());
    }

    // == GET /kos ==
    @Test
    // @WithMockUser // Assuming public access, no specific user needed
    void getKos_Success_Returns200() throws Exception {
        // Mock service findAllKos (or equivalent search without filters)
        List<Kos> allKos = Collections.singletonList(kos);
        when(kosService.findAllKos()).thenReturn(allKos);

        // Perform GET request
        mockMvc.perform(get("/kos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Assert status 200 OK
                // Check response body (assuming ApiResponse structure with data field as a list)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Kos list fetched successfully")) // Adjust expected message
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(kosId))
                .andExpect(jsonPath("$.data[0].nama").value("Test Kos Controller"));

        // Verify service method was called
        verify(kosService, times(1)).findAllKos();
    }

    @Test
    void getKos_WithFilters_Success_Returns200() throws Exception {
        // Define filters
        String keyword = "Test";
        List<Kos> filteredKosList = Collections.singletonList(kos); // Assume 'kos' matches the filter

        // Mock service searchKos (or equivalent) with filter parameters
        // IMPORTANT: Adjust the method signature and parameters in when() and verify() below based on your actual KosService.searchKos method
        when(kosService.searchKos(eq(keyword), any(), any(), any(), any(), any(), any())).thenReturn(filteredKosList); // Example: Adjust any() count/type

        // Perform GET request with query parameters
        mockMvc.perform(get("/kos")
                        .param("keyword", keyword) // Add filter parameter
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Assert status 200 OK
                // Check filtered response body
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Kos list fetched successfully")) // Adjust message
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(kosId));

        // Verify service method was called with correct filter
        // IMPORTANT: Adjust the parameters in verify() below to match the when() call above
        verify(kosService, times(1)).searchKos(eq(keyword), any(), any(), any(), any(), any(), any());
    }

    // == GET /kos/my ==
    @Test
    @WithMockUser(username = ownerId, roles = {"PEMILIK"})
    void getMyKos_Owner_Success_Returns200() throws Exception {
        // Mock service getKosByOwner(ownerId)
        List<Kos> myKos = Collections.singletonList(kos);
        when(kosService.getKosByOwner(eq(ownerId))).thenReturn(myKos);

        // Perform GET request
        mockMvc.perform(get("/kos/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Assert status 200 OK
                // Check response body list
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Owner's Kos list fetched successfully")) // Adjust message
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(kosId));

        // Verify service method was called
        verify(kosService, times(1)).getKosByOwner(eq(ownerId));
    }

    @Test
    @WithMockUser(username = "tenant-user", roles = {"PENYEWA"})
    void getMyKos_Tenant_Unauthorized_Returns403() throws Exception {
        // Perform GET request (no service mock needed)
        mockMvc.perform(get("/kos/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // Assert status 403 Forbidden
    }

    // == GET /kos/{id} ==
    @Test
    // @WithMockUser // Assuming public access
    void getKosById_Success_Returns200() throws Exception {
        // Mock service getKosById(kosId)
        when(kosService.getKosById(eq(kosId))).thenReturn(kos);

        // Perform GET request
        mockMvc.perform(get("/kos/{id}", kosId) // Pass kosId as path variable
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Assert status 200 OK
                // Check response body
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Kos details fetched successfully")) // Adjust message
                .andExpect(jsonPath("$.data.id").value(kosId))
                .andExpect(jsonPath("$.data.nama").value(kos.getNama()));

        // Verify service method was called
        verify(kosService, times(1)).getKosById(eq(kosId));
    }

    @Test
    void getKosById_NotFound_Returns404() throws Exception {
        // Mock service getKosById(nonExistentId) to throw KosNotFoundException
        // Ensure KosNotFoundException is defined and extends appropriate RuntimeException
        when(kosService.getKosById(eq(nonExistentId))).thenThrow(new KosNotFoundException(nonExistentId));

        // Perform GET request
        mockMvc.perform(get("/kos/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Assert status 404 Not Found (assuming @ControllerAdvice handles exception)

        // Verify service method was called
        verify(kosService, times(1)).getKosById(eq(nonExistentId));
    }

    // == PATCH /kos/{id} ==
    @Test
    @WithMockUser(username = ownerId, roles = {"PEMILIK"})
    void patchKos_Owner_Success_Returns200() throws Exception {
        // Prepare update JSON data (e.g., only update the name)
        Kos updatedFields = new Kos();
        String updatedName = "Updated Test Kos";
        updatedFields.setNama(updatedName);

        // Prepare the expected full Kos object after update
        Kos updatedKos = new Kos();
        updatedKos.setId(kosId);
        updatedKos.setPemilik(owner); // Owner shouldn't change
        updatedKos.setNama(updatedName);
        // Copy other fields from original 'kos' if necessary

        // Mock service updateKos(kosId, updatedData, ownerId)
        // Assuming updateKos takes id, partial update object, and ownerId
        when(kosService.updateKos(eq(kosId), any(Kos.class), eq(ownerId))).thenReturn(updatedKos);

        // Perform PATCH request with CSRF
        mockMvc.perform(patch("/kos/{id}", kosId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonKos.write(updatedFields).getJson()) // Send only updated fields
                        .with(csrf()))
                .andExpect(status().isOk()) // Assert status 200 OK
                // Check response body for updated data
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Kos updated successfully")) // Adjust message
                .andExpect(jsonPath("$.data.id").value(kosId))
                .andExpect(jsonPath("$.data.nama").value(updatedName));

        // Verify service method was called
        verify(kosService, times(1)).updateKos(eq(kosId), any(Kos.class), eq(ownerId));
    }

    @Test
    @WithMockUser(username = ownerId, roles = {"PEMILIK"})
    void patchKos_Owner_NotFound_Returns404() throws Exception {
        // Prepare update JSON data
        Kos updatedFields = new Kos();
        updatedFields.setNama("Attempt Update NonExistent");

        // Mock service updateKos to throw KosNotFoundException
        when(kosService.updateKos(eq(nonExistentId), any(Kos.class), eq(ownerId)))
                .thenThrow(new KosNotFoundException(nonExistentId));

        // Perform PATCH request
        mockMvc.perform(patch("/kos/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonKos.write(updatedFields).getJson())
                        .with(csrf()))
                .andExpect(status().isNotFound()); // Assert status 404 Not Found

        // Verify service method was called
        verify(kosService, times(1)).updateKos(eq(nonExistentId), any(Kos.class), eq(ownerId));
    }

    @Test
    @WithMockUser(username = "tenant-user", roles = {"PENYEWA"})
    void patchKos_Tenant_Unauthorized_Returns403() throws Exception {
        // Prepare update JSON data (content doesn't really matter)
        Kos updatedFields = new Kos();
        updatedFields.setNama("Tenant Attempt Update");

        // Perform PATCH request
        mockMvc.perform(patch("/kos/{id}", kosId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonKos.write(updatedFields).getJson())
                        .with(csrf()))
                .andExpect(status().isForbidden()); // Assert status 403 Forbidden
    }

    @Test
    @WithMockUser(username = ownerId, roles = {"PEMILIK"}) // Authenticated as the FIRST owner
    void patchKos_OwnerUpdatesOthersKos_Forbidden_Returns403() throws Exception {
        // Prepare update JSON data
        Kos updatedFields = new Kos();
        updatedFields.setNama("Attempt Update Other's Kos");

        // Mock service updateKos to throw UnauthorizedAccessException
        // Ensure UnauthorizedAccessException is defined and results in 403 via @ControllerAdvice
        when(kosService.updateKos(eq(anotherKosId), any(Kos.class), eq(ownerId)))
                .thenThrow(new UnauthorizedAccessException("Owner cannot update another owner's Kos"));

        // Perform PATCH request targeting another owner's Kos
        mockMvc.perform(patch("/kos/{id}", anotherKosId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonKos.write(updatedFields).getJson())
                        .with(csrf()))
                .andExpect(status().isForbidden()); // Assert status 403 Forbidden

        // Verify service method was called
        verify(kosService, times(1)).updateKos(eq(anotherKosId), any(Kos.class), eq(ownerId));
    }

    // == DELETE /kos/{id} ==
    @Test
    @WithMockUser(username = ownerId, roles = {"PEMILIK"})
    void deleteKos_Owner_Success_Returns204() throws Exception {
        // Mock service deleteKos(kosId, ownerId) - void method
        doNothing().when(kosService).deleteKos(eq(kosId), eq(ownerId));

        // Perform DELETE request with CSRF
        mockMvc.perform(delete("/kos/{id}", kosId) // Pass kosId as path variable
                        .with(csrf()))
                .andExpect(status().isNoContent()); // Assert status 204 No Content

        // Verify service method was called
        verify(kosService, times(1)).deleteKos(eq(kosId), eq(ownerId));
    }

    @Test
    @WithMockUser(username = ownerId, roles = {"PEMILIK"})
    void deleteKos_Owner_NotFound_Returns404() throws Exception {
        // Mock service deleteKos to throw KosNotFoundException
        // Ensure KosNotFoundException is defined and results in 404 via @ControllerAdvice
        doThrow(new KosNotFoundException(nonExistentId)).when(kosService).deleteKos(eq(nonExistentId), eq(ownerId));

        // Perform DELETE request
        mockMvc.perform(delete("/kos/{id}", nonExistentId)
                        .with(csrf()))
                .andExpect(status().isNotFound()); // Assert status 404 Not Found

        // Verify service method was called
        verify(kosService, times(1)).deleteKos(eq(nonExistentId), eq(ownerId));
    }

    @Test
    @WithMockUser(username = "tenant-user", roles = {"PENYEWA"})
    void deleteKos_Tenant_Unauthorized_Returns403() throws Exception {
        // Perform DELETE request
        mockMvc.perform(delete("/kos/{id}", kosId)
                        .with(csrf()))
                .andExpect(status().isForbidden()); // Assert status 403 Forbidden

        // Verify service was NOT called (optional, but good practice)
        verify(kosService, never()).deleteKos(anyString(), anyString());
    }

    @Test
    @WithMockUser(username = ownerId, roles = {"PEMILIK"}) // Authenticated as the FIRST owner
    void deleteKos_OwnerDeletesOthersKos_Forbidden_Returns403() throws Exception {
        // Mock service deleteKos to throw UnauthorizedAccessException
        // Ensure UnauthorizedAccessException is defined and results in 403 via @ControllerAdvice
        doThrow(new UnauthorizedAccessException("Owner cannot delete another owner's Kos"))
                .when(kosService).deleteKos(eq(anotherKosId), eq(ownerId));

        // Perform DELETE request targeting another owner's Kos
        mockMvc.perform(delete("/kos/{id}", anotherKosId)
                        .with(csrf()))
                .andExpect(status().isForbidden()); // Assert status 403 Forbidden

        // Verify service method was called
        verify(kosService, times(1)).deleteKos(eq(anotherKosId), eq(ownerId));
    }
}
