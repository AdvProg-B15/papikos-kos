package id.ac.ui.cs.advprog.papikos.kos.controller;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.service.KosService;
import id.ac.ui.cs.advprog.papikos.kos.exception.KosNotFoundException;
import id.ac.ui.cs.advprog.papikos.kos.exception.UnauthorizedAccessException;
import id.ac.ui.cs.advprog.papikos.kos.response.ApiResponse;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class KosControllerTest {

    @Mock
    private KosService kosService;

    @InjectMocks
    private KosController kosController;

    private Kos kos;
    private Kos anotherKos;
    private UUID kosId;
    private UUID ownerUserId;
    private UUID anotherUserId;
    private UUID nonExistentKosId;
    private Authentication ownerAuth;
    private Authentication anotherOwnerAuth;
    // private Authentication tenantAuth; // Not used in current successful path tests

    @BeforeEach
    void setUp() {
        kosId = UUID.randomUUID();
        ownerUserId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();
        nonExistentKosId = UUID.randomUUID();

        ownerAuth = new UsernamePasswordAuthenticationToken(
                ownerUserId.toString(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("PEMILIK"))
        );

        anotherOwnerAuth = new UsernamePasswordAuthenticationToken(
                anotherUserId.toString(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("PEMILIK"))
        );

        // tenantAuth = new UsernamePasswordAuthenticationToken(
        // UUID.randomUUID().toString(), 
        // null,
        // Collections.singletonList(new SimpleGrantedAuthority("PENYEWA"))
        // );

        kos = new Kos();
        kos.setId(kosId);
        kos.setOwnerUserId(ownerUserId);
        kos.setName("Test Kos Controller");
        kos.setAddress("Jl. Controller Test 1");
        kos.setDescription("Kos for controller testing");
        kos.setNumRooms(5);
        kos.setMonthlyRentPrice(new BigDecimal("1200000.00"));
        kos.setIsListed(true);
        kos.setCreatedAt(LocalDateTime.of(2023, 1, 1, 10, 0, 0)); // Fixed time for consistent testing
        kos.setUpdatedAt(LocalDateTime.of(2023, 1, 2, 11, 0, 0)); // Fixed time for consistent testing

        anotherKos = new Kos();
        anotherKos.setId(UUID.randomUUID()); // Different ID
        anotherKos.setOwnerUserId(anotherUserId);
        anotherKos.setName("Another Owner's Kos");
        anotherKos.setAddress("Jl. Auth Test 99");
        anotherKos.setNumRooms(3);
        anotherKos.setMonthlyRentPrice(new BigDecimal("1000000.00"));
        anotherKos.setIsListed(true);
        anotherKos.setCreatedAt(LocalDateTime.of(2023, 2, 1, 10, 0, 0));
        anotherKos.setUpdatedAt(LocalDateTime.of(2023, 2, 2, 11, 0, 0));
    }

    @Test
    void postKos_Success_Returns201() {
        Kos kosToCreate = new Kos();
        kosToCreate.setName("New Kos Name");
        kosToCreate.setAddress("New Kos Address");
        kosToCreate.setDescription("New Kos Description");
        kosToCreate.setNumRooms(10);
        kosToCreate.setMonthlyRentPrice(new BigDecimal("1500000.00"));
        kosToCreate.setIsListed(false);

        Kos serviceReturnedKos = new Kos();
        serviceReturnedKos.setId(this.kosId); // Use a known ID for clarity, could be different from kosToCreate's potential ID
        serviceReturnedKos.setOwnerUserId(this.ownerUserId); 
        serviceReturnedKos.setName(kosToCreate.getName());
        serviceReturnedKos.setAddress(kosToCreate.getAddress());
        serviceReturnedKos.setDescription(kosToCreate.getDescription());
        serviceReturnedKos.setNumRooms(kosToCreate.getNumRooms());
        serviceReturnedKos.setMonthlyRentPrice(kosToCreate.getMonthlyRentPrice());
        serviceReturnedKos.setIsListed(true); // Example: service behavior lists it
        LocalDateTime fixedCreateTime = LocalDateTime.of(2024, 1, 1, 12,0,0);
        LocalDateTime fixedUpdateTime = LocalDateTime.of(2024, 1, 1, 12,0,1);
        serviceReturnedKos.setCreatedAt(fixedCreateTime);
        serviceReturnedKos.setUpdatedAt(fixedUpdateTime);

        when(kosService.createKos(any(Kos.class), eq(this.ownerUserId))).thenReturn(serviceReturnedKos);

        ResponseEntity<ApiResponse<Kos>> responseEntity = kosController.createKos(kosToCreate, ownerAuth);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        ApiResponse<Kos> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(201, apiResponse.getStatus());
        assertEquals("Kos created successfully", apiResponse.getMessage());
        assertNotNull(apiResponse.getTimestamp());

        Kos responseData = apiResponse.getData();
        assertNotNull(responseData);
        assertSame(serviceReturnedKos, responseData, "Critical: serviceReturnedKos and responseData should be the same instance.");

        // Debugging output and AssertJ for ID
        UUID expectedId = serviceReturnedKos.getId();
        UUID actualId = responseData.getId();
        System.out.println("[postKos_Success_Returns201] Expected ID: " + expectedId + " (Instance: " + System.identityHashCode(expectedId) + ")");
        System.out.println("[postKos_Success_Returns201] Actual ID  : " + actualId + " (Instance: " + System.identityHashCode(actualId) + ")");
        System.out.println("[postKos_Success_Returns201] UUIDs .equals(): " + (expectedId != null && expectedId.equals(actualId)));
        
        assertThat(actualId).as("Checking ID with AssertJ").isEqualTo(expectedId);
        // Fallback to JUnit to ensure it's not AssertJ masking an issue, though AssertJ is usually more informative
        assertEquals(expectedId, actualId, "JUnit assertEquals for ID failed."); 

        assertEquals(serviceReturnedKos.getOwnerUserId(), responseData.getOwnerUserId());
        assertEquals(serviceReturnedKos.getName(), responseData.getName());
        assertEquals(serviceReturnedKos.getAddress(), responseData.getAddress());
        assertEquals(serviceReturnedKos.getDescription(), responseData.getDescription());
        assertEquals(serviceReturnedKos.getNumRooms(), responseData.getNumRooms());
        assertEquals(0, serviceReturnedKos.getMonthlyRentPrice().compareTo(responseData.getMonthlyRentPrice()));
        assertEquals(serviceReturnedKos.getIsListed(), responseData.getIsListed());
        assertEquals(fixedCreateTime, responseData.getCreatedAt());
        assertEquals(fixedUpdateTime, responseData.getUpdatedAt());

        verify(kosService, times(1)).createKos(argThat(k ->
            Objects.equals(k.getName(), kosToCreate.getName()) &&
            Objects.equals(k.getAddress(), kosToCreate.getAddress()) &&
            Objects.equals(k.getDescription(), kosToCreate.getDescription()) &&
            Objects.equals(k.getNumRooms(), kosToCreate.getNumRooms()) && 
            (k.getMonthlyRentPrice() == null ? kosToCreate.getMonthlyRentPrice() == null : 
                (kosToCreate.getMonthlyRentPrice() != null && k.getMonthlyRentPrice().compareTo(kosToCreate.getMonthlyRentPrice()) == 0)) && 
            Objects.equals(k.getIsListed(), kosToCreate.getIsListed()) && 
            k.getId() == null &&
            k.getOwnerUserId() == null
        ), eq(this.ownerUserId));
    }

    @Test
    void postKos_NullAuthentication_ThrowsIllegalStateException() {
        Kos kosToCreate = new Kos();
        kosToCreate.setName("Test Kos");
        Exception exception = assertThrows(IllegalStateException.class, () -> kosController.createKos(kosToCreate, null));
        assertEquals("Authentication principal is required but missing.", exception.getMessage());
        verify(kosService, never()).createKos(any(Kos.class), any(UUID.class));
    }

    @Test
    void postKos_InvalidAuthPrincipalFormat_ThrowsIllegalArgumentException() {
        Kos kosToCreate = new Kos();
        kosToCreate.setName("Test Kos");
        Authentication invalidAuth = new UsernamePasswordAuthenticationToken(
            "not-a-uuid", null, Collections.singletonList(new SimpleGrantedAuthority("PEMILIK"))
        );
        Exception exception = assertThrows(IllegalArgumentException.class, () -> kosController.createKos(kosToCreate, invalidAuth));
        assertTrue(exception.getMessage().contains("Invalid user identifier format"));
        verify(kosService, never()).createKos(any(Kos.class), any(UUID.class));
    }

    @Test
    void getAllKos_Success_Returns200() {
        List<Kos> allKosList = Arrays.asList(kos, anotherKos);
        when(kosService.findAllKos()).thenReturn(allKosList);

        ResponseEntity<ApiResponse<List<Kos>>> responseEntity = kosController.getAllKos();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ApiResponse<List<Kos>> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(200, apiResponse.getStatus());
        assertEquals("Kos list fetched successfully", apiResponse.getMessage());
        List<Kos> responseData = apiResponse.getData();
        assertNotNull(responseData);
        assertSame(allKosList, responseData);
        assertEquals(2, responseData.size());
        
        Kos firstKosInResponse = responseData.get(0);
        assertSame(kos, firstKosInResponse);
        assertEquals(kos.getId(), firstKosInResponse.getId());
        assertEquals(kos.getName(), firstKosInResponse.getName());
        assertEquals(kos.getAddress(), firstKosInResponse.getAddress());
        assertEquals(kos.getOwnerUserId(), firstKosInResponse.getOwnerUserId());

        Kos secondKosInResponse = responseData.get(1);
        assertSame(anotherKos, secondKosInResponse);
        assertEquals(anotherKos.getId(), secondKosInResponse.getId());
        assertEquals(anotherKos.getName(), secondKosInResponse.getName());
        assertEquals(anotherKos.getAddress(), secondKosInResponse.getAddress());
        assertEquals(anotherKos.getOwnerUserId(), secondKosInResponse.getOwnerUserId());

        verify(kosService, times(1)).findAllKos();
    }

    @Test
    void searchKos_WithKeyword_Success_Returns200() {
        String keyword = "Controller Test";
        List<Kos> searchResult = Collections.singletonList(kos);
        when(kosService.findAllKos()).thenReturn(searchResult);

        ResponseEntity<ApiResponse<List<Kos>>> responseEntity = kosController.getAllKos();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ApiResponse<List<Kos>> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(200, apiResponse.getStatus());
        assertEquals("Kos list fetched successfully", apiResponse.getMessage());
        List<Kos> responseData = apiResponse.getData();
        assertNotNull(responseData);
        assertSame(searchResult, responseData);
        assertEquals(1, responseData.size());
        
        Kos actualKos = responseData.get(0);
        assertSame(kos, actualKos);
        assertEquals(kos.getId(), actualKos.getId());
        assertEquals(kos.getName(), actualKos.getName());
        assertEquals(kos.getOwnerUserId(), actualKos.getOwnerUserId());

        verify(kosService, times(1)).findAllKos();
    }

    @Test
    void getMyKos_Owner_Success_Returns200() {
        List<Kos> myKosList = Collections.singletonList(kos);
        when(kosService.findKosByOwnerUserId(eq(ownerUserId))).thenReturn(myKosList);

        ResponseEntity<ApiResponse<List<Kos>>> responseEntity = kosController.getMyKos(ownerAuth);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ApiResponse<List<Kos>> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(200, apiResponse.getStatus());
        assertEquals("Owner's Kos list fetched successfully", apiResponse.getMessage());
        List<Kos> responseData = apiResponse.getData();
        assertNotNull(responseData);
        assertSame(myKosList, responseData);
        assertEquals(1, responseData.size());

        Kos actualKos = responseData.get(0);
        assertSame(kos, actualKos, "Critical: kos from setUp and actualKos from response should be the same instance.");
        assertEquals(kos.getId(), actualKos.getId());
        assertEquals(kos.getOwnerUserId(), actualKos.getOwnerUserId());
        assertEquals(kos.getName(), actualKos.getName());
        assertEquals(kos.getAddress(), actualKos.getAddress());
        assertEquals(kos.getDescription(), actualKos.getDescription());
        assertEquals(kos.getNumRooms(), actualKos.getNumRooms());
        assertEquals(0, kos.getMonthlyRentPrice().compareTo(actualKos.getMonthlyRentPrice()));
        assertEquals(kos.getIsListed(), actualKos.getIsListed());

        // Debugging for CreatedAt
        LocalDateTime expectedCreatedAt = kos.getCreatedAt();
        LocalDateTime actualCreatedAt = actualKos.getCreatedAt();
        System.out.println("[getMyKos_Owner_Success_Returns200] Expected CreatedAt: " + expectedCreatedAt + " (Instance: " + System.identityHashCode(expectedCreatedAt) + ")");
        System.out.println("[getMyKos_Owner_Success_Returns200] Actual CreatedAt  : " + actualCreatedAt + " (Instance: " + System.identityHashCode(actualCreatedAt) + ")");
        System.out.println("[getMyKos_Owner_Success_Returns200] LocalDateTimes .equals(): " + (expectedCreatedAt != null && expectedCreatedAt.equals(actualCreatedAt)));

        assertEquals(expectedCreatedAt, actualCreatedAt, "JUnit assertEquals for CreatedAt failed.");
        assertEquals(kos.getUpdatedAt(), actualKos.getUpdatedAt());

        verify(kosService, times(1)).findKosByOwnerUserId(eq(ownerUserId));
    }

    @Test
    void getKosById_Success_Returns200() {
        when(kosService.findKosById(eq(kosId))).thenReturn(kos);

        ResponseEntity<ApiResponse<Kos>> responseEntity = kosController.getKosById(kosId);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ApiResponse<Kos> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(200, apiResponse.getStatus());
        assertEquals("Kos details fetched successfully", apiResponse.getMessage());
        Kos responseData = apiResponse.getData();
        assertNotNull(responseData);
        assertSame(kos, responseData);

        assertEquals(kos.getId(), responseData.getId());
        assertEquals(kos.getName(), responseData.getName());
        assertEquals(kos.getAddress(), responseData.getAddress());
        assertEquals(kos.getDescription(), responseData.getDescription());
        assertEquals(kos.getNumRooms(), responseData.getNumRooms());
        assertEquals(0, kos.getMonthlyRentPrice().compareTo(responseData.getMonthlyRentPrice()));
        assertEquals(kos.getIsListed(), responseData.getIsListed());
        assertEquals(kos.getCreatedAt(), responseData.getCreatedAt());
        assertEquals(kos.getUpdatedAt(), responseData.getUpdatedAt());

        verify(kosService, times(1)).findKosById(eq(kosId));
    }

    @Test
    void getKosById_NotFound_ThrowsKosNotFoundException() {
        when(kosService.findKosById(eq(nonExistentKosId))).thenThrow(new KosNotFoundException(nonExistentKosId));
        KosNotFoundException exception = assertThrows(KosNotFoundException.class, () -> kosController.getKosById(nonExistentKosId));
        assertEquals("Kos with ID " + nonExistentKosId + " not found", exception.getMessage());
        verify(kosService, times(1)).findKosById(eq(nonExistentKosId));
    }

    @Test
    void updateKos_Owner_Success_Returns200() throws BadRequestException {
        Kos kosUpdateData = new Kos(); 
        kosUpdateData.setName("Updated Kos Name From Test");
        kosUpdateData.setNumRooms(12);
        kosUpdateData.setIsListed(false); // Explicitly set for update DTO

        Kos updatedServiceResultKos = new Kos(); 
        updatedServiceResultKos.setId(this.kosId);
        updatedServiceResultKos.setOwnerUserId(this.ownerUserId);
        updatedServiceResultKos.setName(kosUpdateData.getName()); 
        updatedServiceResultKos.setNumRooms(kosUpdateData.getNumRooms()); 
        updatedServiceResultKos.setAddress(this.kos.getAddress()); // Assuming address is not updated by this payload
        updatedServiceResultKos.setDescription(this.kos.getDescription()); // Assuming desc is not updated
        updatedServiceResultKos.setMonthlyRentPrice(this.kos.getMonthlyRentPrice()); // Assuming price not updated
        updatedServiceResultKos.setIsListed(kosUpdateData.getIsListed()); // Updated value from payload
        LocalDateTime fixedUpdateTime = LocalDateTime.of(2024, 1, 2, 12, 0, 0);
        updatedServiceResultKos.setUpdatedAt(fixedUpdateTime); 
        updatedServiceResultKos.setCreatedAt(this.kos.getCreatedAt()); // CreatedAt should remain original

        when(kosService.updateKos(eq(this.kosId), any(Kos.class), eq(this.ownerUserId))).thenReturn(updatedServiceResultKos);

        ResponseEntity<ApiResponse<Kos>> responseEntity = kosController.updateKos(this.kosId, kosUpdateData, ownerAuth);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ApiResponse<Kos> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(200, apiResponse.getStatus());
        assertEquals("Kos updated successfully", apiResponse.getMessage());

        Kos responseData = apiResponse.getData();
        assertNotNull(responseData);
        assertSame(updatedServiceResultKos, responseData);

        assertEquals(updatedServiceResultKos.getId(), responseData.getId());
        assertEquals(updatedServiceResultKos.getName(), responseData.getName());
        assertEquals(updatedServiceResultKos.getNumRooms(), responseData.getNumRooms());
        assertEquals(updatedServiceResultKos.getAddress(), responseData.getAddress());
        assertEquals(updatedServiceResultKos.getDescription(), responseData.getDescription());
        assertEquals(0, updatedServiceResultKos.getMonthlyRentPrice().compareTo(responseData.getMonthlyRentPrice()));
        assertEquals(updatedServiceResultKos.getIsListed(), responseData.getIsListed());
        assertEquals(fixedUpdateTime, responseData.getUpdatedAt());
        assertEquals(this.kos.getCreatedAt(), responseData.getCreatedAt());

        verify(kosService, times(1)).updateKos(eq(this.kosId), argThat(k ->
            Objects.equals(k.getName(), kosUpdateData.getName()) &&
            Objects.equals(k.getNumRooms(), kosUpdateData.getNumRooms()) &&
            Objects.equals(k.getIsListed(), kosUpdateData.getIsListed()) && // Check isListed from payload
            // Fields not in kosUpdateData explicitly should be null if service expects a pure DTO
            k.getAddress() == null &&
            k.getDescription() == null &&
            k.getMonthlyRentPrice() == null &&
            k.getId() == null &&
            k.getOwnerUserId() == null
        ), eq(this.ownerUserId));
    }

    @Test
    void updateKos_NotFound_ThrowsKosNotFoundException() {
        Kos kosUpdateData = new Kos();
        kosUpdateData.setName("Update attempt on non-existent Kos");
        when(kosService.updateKos(eq(nonExistentKosId), any(Kos.class), eq(ownerUserId)))
                .thenThrow(new KosNotFoundException(nonExistentKosId));
        KosNotFoundException exception = assertThrows(KosNotFoundException.class, () -> kosController.updateKos(nonExistentKosId, kosUpdateData, ownerAuth));
        assertEquals("Kos with ID " + nonExistentKosId + " not found", exception.getMessage());
        verify(kosService, times(1)).updateKos(eq(nonExistentKosId), any(Kos.class), eq(ownerUserId));
    }

    @Test
    void updateKos_UnauthorizedAccess_ThrowsUnauthorizedAccessException() {
        Kos kosUpdateData = new Kos();
        kosUpdateData.setName("Update attempt by wrong owner");
        String expectedErrorMessage = "User " + anotherUserId + " is not authorized to update Kos " + kosId;
        when(kosService.updateKos(eq(kosId), any(Kos.class), eq(anotherUserId)))
                .thenThrow(new UnauthorizedAccessException(expectedErrorMessage));
        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> kosController.updateKos(kosId, kosUpdateData, anotherOwnerAuth));
        assertEquals(expectedErrorMessage, exception.getMessage());
        verify(kosService, times(1)).updateKos(eq(kosId), any(Kos.class), eq(anotherUserId));
    }

    @Test
    void deleteKos_Owner_Success_Returns204() {
        doNothing().when(kosService).deleteKos(eq(kosId), eq(ownerUserId));
        ResponseEntity<Void> responseEntity = kosController.deleteKos(kosId, ownerAuth);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        verify(kosService, times(1)).deleteKos(eq(kosId), eq(ownerUserId));
    }

    @Test
    void deleteKos_NotFound_ThrowsKosNotFoundException() {
        doThrow(new KosNotFoundException(nonExistentKosId))
            .when(kosService).deleteKos(eq(nonExistentKosId), eq(ownerUserId));
        KosNotFoundException exception = assertThrows(KosNotFoundException.class, () -> kosController.deleteKos(nonExistentKosId, ownerAuth));
        assertEquals("Kos with ID " + nonExistentKosId + " not found", exception.getMessage());
        verify(kosService, times(1)).deleteKos(eq(nonExistentKosId), eq(ownerUserId));
    }

    @Test
    void deleteKos_UnauthorizedAccess_ThrowsUnauthorizedAccessException() {
        String expectedErrorMessage = "User " + anotherUserId + " is not authorized to delete Kos " + kosId;
        doThrow(new UnauthorizedAccessException(expectedErrorMessage))
            .when(kosService).deleteKos(eq(kosId), eq(anotherUserId));
        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> kosController.deleteKos(kosId, anotherOwnerAuth));
        assertEquals(expectedErrorMessage, exception.getMessage());
        verify(kosService, times(1)).deleteKos(eq(kosId), eq(anotherUserId));
    }
}
