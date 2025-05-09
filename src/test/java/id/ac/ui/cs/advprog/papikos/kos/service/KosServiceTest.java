package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.repository.KosRepository;
import id.ac.ui.cs.advprog.papikos.kos.exception.KosNotFoundException;
import id.ac.ui.cs.advprog.papikos.kos.exception.UnauthorizedAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KosServiceTest {

    @Mock
    private KosRepository kosRepository;

    @InjectMocks
    private KosServiceImpl kosService;

    private Kos kos;
    private UUID kosId;
    private UUID ownerUserId;
    private UUID anotherUserId;

    @BeforeEach
    void setUp() {
        kosId = UUID.randomUUID();
        ownerUserId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID(); // For unauthorized tests

        kos = new Kos();
        kos.setId(kosId);
        kos.setOwnerUserId(ownerUserId);
        kos.setName("Kos Test ABC");
        kos.setAddress("Jl. Test No. 123");
        kos.setDescription("Kos nyaman dekat kampus");
        kos.setNumRooms(10);
        kos.setMonthlyRentPrice(new BigDecimal("1500000.00"));
        kos.setIsListed(true);
        kos.setCreatedAt(LocalDateTime.now().minusDays(1));
        kos.setUpdatedAt(LocalDateTime.now().minusHours(1));
    }

    // --- CREATE ---
    @Test
    void createKos_Success() {
        Kos newKos = new Kos();
        newKos.setOwnerUserId(String.valueOf(ownerUserId));
        newKos.setName("Kos Baru Mantap");
        newKos.setAddress("Jl. Baru No. 1");
        newKos.setDescription("Deskripsi Kos Baru");
        newKos.setNumRooms(5);
        newKos.setMonthlyRentPrice(new BigDecimal("2000000.00"));

        // When save is called, return the kos with ID and timestamps assigned (simulate JPA)
        when(kosRepository.save(any(Kos.class))).thenAnswer(invocation -> {
            Kos savedKos = invocation.getArgument(0);
            savedKos.setId(UUID.randomUUID()); // Simulate ID generation
            savedKos.setCreatedAt(LocalDateTime.now()); // Simulate @PrePersist
            savedKos.setUpdatedAt(LocalDateTime.now()); // Simulate @PrePersist
            return savedKos;
        });

        // Act: Call the service method to create Kos
        Kos createdKos = kosService.createKos(newKos, ownerUserId); // Assuming service sets ownerUserId

        // Assert: Check the returned Kos object
        assertNotNull(createdKos);
        assertNotNull(createdKos.getId());
        assertEquals(ownerUserId, createdKos.getOwnerUserId());
        assertEquals("Kos Baru Mantap", createdKos.getName());
        assertEquals("Jl. Baru No. 1", createdKos.getAddress());
        assertEquals(5, createdKos.getNumRooms());
        assertEquals(0, new BigDecimal("2000000.00").compareTo(createdKos.getMonthlyRentPrice()));
        assertTrue(createdKos.getIsListed());
        assertNotNull(createdKos.getCreatedAt());
        assertNotNull(createdKos.getUpdatedAt());

        // Assert: Verify repository save was called exactly once
        verify(kosRepository, times(1)).save(any(Kos.class));
    }

    @Test
    void createKos_ThrowsException_WhenNameIsNull() {
        // Arrange: Create Kos with invalid data (null name)
        Kos invalidKos = new Kos();
        invalidKos.setOwnerUserId(ownerUserId);
        invalidKos.setName(null); // Invalid state
        invalidKos.setAddress("Jl. Invalid");
        invalidKos.setNumRooms(1);
        invalidKos.setMonthlyRentPrice(BigDecimal.ONE);

        // Act & Assert: Expect an exception (e.g., IllegalArgumentException or ConstraintViolationException if using validation)
        // For this example, let's assume the service throws IllegalArgumentException for basic checks
        assertThrows(IllegalArgumentException.class, () -> {
            kosService.createKos(invalidKos, ownerUserId);
        }, "Kos name cannot be null or empty"); // Adjust expected exception and message based on actual service implementation

        // Assert: Verify repository save was *not* called
        verify(kosRepository, never()).save(any(Kos.class));
    }


    // --- READ ---
    @Test
    void findKosById_Success() {
        // Arrange: Mock the repository to return the kos object when findById is called
        when(kosRepository.findById(kosId)).thenReturn(Optional.of(kos));

        // Act: Call the service method
        Kos foundKos = kosService.findKosById(kosId);

        // Assert: Check if the correct kos object is returned
        assertNotNull(foundKos);
        assertEquals(kosId, foundKos.getId());
        assertEquals(kos.getName(), foundKos.getName());
        assertEquals(ownerUserId, foundKos.getOwnerUserId());
        verify(kosRepository, times(1)).findById(kosId); // Verify findById was called once
    }

    @Test
    void findKosById_NotFound() {
        // Arrange: Mock the repository to return an empty Optional
        UUID nonExistentId = UUID.randomUUID();
        when(kosRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert: Check if KosNotFoundException is thrown
        Exception exception = assertThrows(KosNotFoundException.class, () -> {
            kosService.findKosById(nonExistentId);
        });

        // Optionally check the exception message
        String expectedMessage = "Kos with ID " + nonExistentId + " not found";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));

        verify(kosRepository, times(1)).findById(nonExistentId); // Verify findById was called
    }

    @Test
    void findKosByOwnerUserId_Success() {
        // Arrange: Mock repository to return a list containing the kos
        List<Kos> expectedKosList = List.of(kos);
        // Assuming repository method is findByOwnerUserId
        when(kosRepository.findKosByOwnerUserId(ownerUserId)).thenReturn(expectedKosList);

        // Act: Call the service method
        List<Kos> actualKosList = kosService.findKosByOwnerUserId(ownerUserId);

        // Assert: Check if the correct list is returned
        assertNotNull(actualKosList);
        assertEquals(1, actualKosList.size());
        assertEquals(kos, actualKosList.getFirst());
        verify(kosRepository, times(1)).findKosByOwnerUserId(ownerUserId);
    }

    @Test
    void findKosByOwnerUserId_NoKosFound() {
        // Arrange: Mock repository to return an empty list for a different owner
        UUID userWithNoKos = UUID.randomUUID();
        when(kosRepository.findKosByOwnerUserId(userWithNoKos)).thenReturn(Collections.emptyList());

        // Act: Call the service method
        List<Kos> actualKosList = kosService.findKosByOwnerUserId(userWithNoKos);

        // Assert: Check if an empty list is returned
        assertNotNull(actualKosList);
        assertTrue(actualKosList.isEmpty());
        verify(kosRepository, times(1)).findKosByOwnerUserId(userWithNoKos);
    }

    @Test
    void findAllKos_Success() {
        // Arrange: Prepare another Kos object for the list
        Kos anotherKos = new Kos();
        anotherKos.setId(UUID.randomUUID());
        anotherKos.setOwnerUserId(anotherUserId);
        anotherKos.setName("Kos Lain");
        // ... set other properties ...

        List<Kos> expectedKosList = List.of(kos, anotherKos);
        when(kosRepository.findAll()).thenReturn(expectedKosList);

        // Act
        List<Kos> actualKosList = kosService.findAllKos();

        // Assert
        assertNotNull(actualKosList);
        assertEquals(2, actualKosList.size());
        assertTrue(actualKosList.contains(kos));
        assertTrue(actualKosList.contains(anotherKos));
        verify(kosRepository, times(1)).findAll();
    }


    // --- UPDATE ---
    @Test
    void updateKos_Success() {
        // Arrange: Prepare updated data
        Kos updatedKosData = new Kos();
        updatedKosData.setName("Updated Kos Name");
        updatedKosData.setAddress("Updated Address");
        updatedKosData.setDescription("Updated Description");
        updatedKosData.setNumRooms(12);
        updatedKosData.setMonthlyRentPrice(new BigDecimal("1600000.00"));
        updatedKosData.setIsListed(false);

        // Arrange: Mock finding the existing kos
        when(kosRepository.findById(kosId)).thenReturn(Optional.of(kos));
        // Arrange: Mock saving the updated kos - return the updated entity
        when(kosRepository.save(any(Kos.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Call the service method with the correct owner ID
        Kos resultKos = kosService.updateKos(kosId, updatedKosData, ownerUserId);

        // Assert: Verify the result and interactions
        assertNotNull(resultKos);
        assertEquals(kosId, resultKos.getId());
        assertEquals(ownerUserId, resultKos.getOwnerUserId());
        assertEquals("Updated Kos Name", resultKos.getName());
        assertEquals("Updated Address", resultKos.getAddress());
        assertEquals("Updated Description", resultKos.getDescription());
        assertEquals(12, resultKos.getNumRooms());
        assertEquals(0, new BigDecimal("1600000.00").compareTo(resultKos.getMonthlyRentPrice()));
        assertFalse(resultKos.getIsListed());
        assertNotNull(resultKos.getUpdatedAt());
        assertNotNull(resultKos.getCreatedAt());

        verify(kosRepository, times(1)).findById(kosId); // Verify find was called
        verify(kosRepository, times(1)).save(any(Kos.class)); // Verify save was called
    }

    @Test
    void updateKos_NotFound() {
        // Arrange: Prepare updated data
        Kos updatedKosData = new Kos();
        updatedKosData.setName("Updated Kos Name");
        UUID nonExistentId = UUID.randomUUID();

        // Arrange: Mock finding non-existent kos
        when(kosRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert: Check for KosNotFoundException
        assertThrows(KosNotFoundException.class, () -> {
            kosService.updateKos(nonExistentId, updatedKosData, ownerUserId);
        });

        // Verify find was called, but save was not
        verify(kosRepository, times(1)).findById(nonExistentId);
        verify(kosRepository, never()).save(any(Kos.class));
    }

    @Test
    void updateKos_Unauthorized() {
        // Arrange: Prepare updated data
        Kos updatedKosData = new Kos();
        updatedKosData.setName("Updated Kos Name");

        // Arrange: Mock finding the existing kos (owned by 'ownerUserId')
        when(kosRepository.findById(kosId)).thenReturn(Optional.of(kos));

        // Act & Assert: Call the service method with the *wrong* user ID and check for exception
        assertThrows(UnauthorizedAccessException.class, () -> {
            // Attempting update with anotherUserId, but kos is owned by ownerUserId
            kosService.updateKos(kosId, updatedKosData, anotherUserId);
        });

        // Verify find was called, but save was not
        verify(kosRepository, times(1)).findById(kosId);
        verify(kosRepository, never()).save(any(Kos.class));
    }


    // --- DELETE ---
    @Test
    void deleteKos_Success() {
        // Arrange: Mock finding the existing kos
        when(kosRepository.findById(kosId)).thenReturn(Optional.of(kos));
        // Arrange: Mock the delete action (void method, so use doNothing)
        doNothing().when(kosRepository).deleteById(kosId); // Assuming deleteById is used

        // Act: Call the service method with the correct owner ID
        kosService.deleteKos(kosId, ownerUserId);

        // Assert: Verify find and delete were called
        verify(kosRepository, times(1)).findById(kosId);
        verify(kosRepository, times(1)).deleteById(kosId);
    }

    @Test
    void deleteKos_NotFound() {
        // Arrange: Mock finding non-existent kos
        UUID nonExistentId = UUID.randomUUID();
        when(kosRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert: Check for KosNotFoundException
        assertThrows(KosNotFoundException.class, () -> {
            kosService.deleteKos(nonExistentId, ownerUserId);
        });

        // Verify find was called, but delete was not
        verify(kosRepository, times(1)).findById(nonExistentId);
        verify(kosRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void deleteKos_Unauthorized() {
        // Arrange: Mock finding the existing kos (owned by 'ownerUserId')
        when(kosRepository.findById(kosId)).thenReturn(Optional.of(kos));

        // Act & Assert: Call the service method with the *wrong* user ID and check for exception
        assertThrows(UnauthorizedAccessException.class, () -> {
            kosService.deleteKos(kosId, anotherUserId);
        });

        // Verify find was called, but delete was not
        verify(kosRepository, times(1)).findById(kosId);
        verify(kosRepository, never()).deleteById(any(UUID.class));
    }

    // --- SEARCH (Optional, based on old test) ---
    @Test
    void searchKos_Success() {
        // Arrange: Prepare search keyword and expected results
        String keyword = "Test";
        List<Kos> expectedResults = List.of(kos); // Assuming 'kos' matches 'Test'

        // Arrange: Mock repository search method
        when(kosRepository.findKosByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, keyword))
                .thenReturn(expectedResults);

        // Act: Call the service search method
        List<Kos> actualResults = kosService.searchKos(keyword); // Assuming this service method exists

        // Assert: Verify the results
        assertNotNull(actualResults);
        assertEquals(expectedResults.size(), actualResults.size());
        assertEquals(expectedResults.getFirst(), actualResults.getFirst());
        verify(kosRepository, times(1)).findKosByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, keyword);
    }

    @Test
    void searchKos_NoResults() {
        // Arrange: Prepare search keyword
        String keyword = "NotFoundKeyword";
        // Arrange: Mock repository search method to return empty list
        when(kosRepository.findKosByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, keyword))
                .thenReturn(Collections.emptyList());

        // Act: Call the service search method
        List<Kos> actualResults = kosService.searchKos(keyword);

        // Assert: Verify the list is empty
        assertNotNull(actualResults);
        assertTrue(actualResults.isEmpty());
        verify(kosRepository, times(1)).findKosByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, keyword);
    }
}
