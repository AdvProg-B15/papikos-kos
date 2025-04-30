package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.Pemilik; // Assuming Pemilik is the Owner model
import id.ac.ui.cs.advprog.papikos.kos.repository.KosRepository;
import id.ac.ui.cs.advprog.papikos.kos.repository.PemilikRepository;
import id.ac.ui.cs.advprog.papikos.kos.exception.KosNotFoundException; // Assuming this exception exists
import id.ac.ui.cs.advprog.papikos.kos.exception.UnauthorizedAccessException; // Assuming this exception exists
import jakarta.validation.ValidationException; // Assuming standard validation
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KosServiceTest {

    @Mock
    private KosRepository kosRepository;

    @Mock
    private PemilikRepository pemilikRepository; // Assuming PemilikRepository handles Owner data

    @InjectMocks
    private KosService kosService;

    private Pemilik owner;
    private Kos kos;
    private String kosId = "kos-123";
    private String ownerId = "owner-456";
    private Pemilik anotherOwner;
    private String anotherOwnerId = "owner-789";

    @BeforeEach
    void setUp() {
        // Initialize common test objects
        owner = new Pemilik(); // Create a Pemilik instance
        owner.setId(ownerId);
        owner.setApproved(true); // Assuming Pemilik has an approval status & setter

        kos = new Kos();
        kos.setId(kosId);
        kos.setPemilik(owner); // Assuming Kos has a reference to Pemilik
        kos.setNama("Kos Test");
        // Set other necessary Kos properties

        // Second owner for unauthorized tests
        anotherOwner = new Pemilik();
        anotherOwner.setId(anotherOwnerId);
        anotherOwner.setApproved(true);
    }

    @Test
    void addKos_Success() {
        // Arrange: Mock owner repository to return the approved owner
        when(pemilikRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        // Arrange: Mock kos repository save to return the saved kos
        when(kosRepository.save(any(Kos.class))).thenReturn(kos);

        // Act: Call the service method
        Kos savedKos = kosService.addKos(kos, ownerId); // Assuming this method exists

        // Assert: Check if the kos was saved and returned correctly
        assertNotNull(savedKos);
        assertEquals(kos.getId(), savedKos.getId());
        assertEquals(owner, savedKos.getPemilik()); // Check if owner is set correctly
        verify(pemilikRepository, times(1)).findById(ownerId); // Verify owner check
        verify(kosRepository, times(1)).save(kos); // Verify save was called
    }

    @Test
    void addKos_FailsIfUserIsNotApprovedOwner() {
        // Arrange: Set owner to not approved
        owner.setApproved(false);
        // Arrange: Mock owner repository to return the unapproved owner
        when(pemilikRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        // Act & Assert: Check for UnauthorizedAccessException (or similar)
        assertThrows(UnauthorizedAccessException.class, () -> { // Assuming this exception
            kosService.addKos(kos, ownerId);
        });

        // Verify owner check was done, but save was not called
        verify(pemilikRepository, times(1)).findById(ownerId);
        verify(kosRepository, never()).save(any(Kos.class));
    }

    @Test
    void addKos_InvalidInput() {
        // Arrange: Create an invalid Kos object (e.g., null name)
        Kos invalidKos = new Kos();
        invalidKos.setPemilik(owner); // Set owner, assuming validation happens after owner check
        // invalidKos.setNama(null); // Example of invalid data

        // Arrange: Mock owner repository to return the approved owner (owner check passes)
        when(pemilikRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        // Arrange: Mock the service method call for invalid input to throw ValidationException
        // This often requires mocking the validation mechanism itself or assuming the service method throws it directly
        // For simplicity here, we'll assume the service method throws it if input is invalid
        // A more robust test might involve mocking a Validator instance if used explicitly
        // Let's assume addKos internally validates and throws:
        doThrow(new ValidationException("Invalid Kos data")).when(kosService).validateKosInput(invalidKos); // Hypothetical validation method
        // OR If addKos does validation directly:
        // We expect addKos itself to throw the exception

        // Act & Assert: Check for ValidationException
        assertThrows(ValidationException.class, () -> {
            kosService.addKos(invalidKos, ownerId); // Call the actual method
        });

        // Verify owner check was done, but save was not called
        verify(pemilikRepository, times(1)).findById(ownerId);
        verify(kosRepository, never()).save(any(Kos.class));
    }

    @Test
    void getKosById_Success() {
        // Arrange: Mock the repository to return the kos object when findById is called
        when(kosRepository.findById(kosId)).thenReturn(Optional.of(kos));

        // Act: Call the service method
        Kos foundKos = kosService.getKosById(kosId); // Assuming this method exists

        // Assert: Check if the correct kos object is returned
        assertNotNull(foundKos);
        assertEquals(kosId, foundKos.getId());
        assertEquals(kos.getNama(), foundKos.getNama());
        assertEquals(owner, foundKos.getPemilik());
        verify(kosRepository, times(1)).findById(kosId); // Verify findById was called once
    }

    @Test
    void getKosById_NotFound() {
        // Arrange: Mock the repository to return an empty Optional
        when(kosRepository.findById(anyString())).thenReturn(Optional.empty());
        String nonExistentId = "non-existent-id";

        // Act & Assert: Check if the correct exception is thrown when the ID doesn't exist
        Exception exception = assertThrows(KosNotFoundException.class, () -> { // Assuming KosNotFoundException
            kosService.getKosById(nonExistentId);
        });

        // Optionally check the exception message
        // String expectedMessage = "Kos with ID " + nonExistentId + " not found";
        // String actualMessage = exception.getMessage();
        // assertTrue(actualMessage.contains(expectedMessage));

        verify(kosRepository, times(1)).findById(nonExistentId); // Verify findById was called
    }

    @Test
    void getKosByOwner_Success() {
        // Arrange: Mock repository to return a list containing the kos
        List<Kos> expectedKosList = List.of(kos);
        when(kosRepository.findByPemilikId(ownerId)).thenReturn(expectedKosList); // Assuming this repository method exists

        // Act: Call the service method
        List<Kos> actualKosList = kosService.getKosByOwner(ownerId); // Assuming this service method exists

        // Assert: Check if the correct list is returned
        assertNotNull(actualKosList);
        assertEquals(1, actualKosList.size());
        assertEquals(kos, actualKosList.get(0));
        verify(kosRepository, times(1)).findByPemilikId(ownerId);
    }

    @Test
    void getKosByOwner_NotFound() {
        // Arrange: Mock repository to return an empty list
        when(kosRepository.findByPemilikId(ownerId)).thenReturn(Collections.emptyList());

        // Act: Call the service method
        List<Kos> actualKosList = kosService.getKosByOwner(ownerId);

        // Assert: Check if an empty list is returned
        assertNotNull(actualKosList);
        assertTrue(actualKosList.isEmpty());
        verify(kosRepository, times(1)).findByPemilikId(ownerId);
    }

    @Test
    void updateKos_Success() {
        // Arrange: Prepare updated data
        Kos updatedKosData = new Kos();
        updatedKosData.setNama("Updated Kos Name");
        // Set other fields to update...

        // Arrange: Mock finding the existing kos
        when(kosRepository.findById(kosId)).thenReturn(Optional.of(kos));
        // Arrange: Mock saving the updated kos - return value might be the saved entity
        when(kosRepository.save(any(Kos.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Call the service method with the correct owner ID
        Kos resultKos = kosService.updateKos(kosId, updatedKosData, ownerId); // Assuming this method exists

        // Assert: Verify the result and interactions
        assertNotNull(resultKos);
        assertEquals(kosId, resultKos.getId()); // ID should remain the same
        assertEquals("Updated Kos Name", resultKos.getNama()); // Check updated field
        assertEquals(owner, resultKos.getPemilik()); // Owner should remain the same

        verify(kosRepository, times(1)).findById(kosId); // Verify find was called
        verify(kosRepository, times(1)).save(any(Kos.class)); // Verify save was called
    }

    @Test
    void updateKos_NotFound() {
        // Arrange: Prepare updated data
        Kos updatedKosData = new Kos();
        updatedKosData.setNama("Updated Kos Name");
        String nonExistentId = "non-existent-id";

        // Arrange: Mock finding non-existent kos
        when(kosRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert: Check for KosNotFoundException
        assertThrows(KosNotFoundException.class, () -> {
            kosService.updateKos(nonExistentId, updatedKosData, ownerId);
        });

        // Verify find was called, but save was not
        verify(kosRepository, times(1)).findById(nonExistentId);
        verify(kosRepository, never()).save(any(Kos.class));
    }

    @Test
    void updateKos_Unauthorized() {
        // Arrange: Prepare updated data
        Kos updatedKosData = new Kos();
        updatedKosData.setNama("Updated Kos Name");

        // Arrange: Mock finding the existing kos (owned by 'owner')
        when(kosRepository.findById(kosId)).thenReturn(Optional.of(kos));

        // Act & Assert: Call the service method with the *wrong* owner ID and check for exception
        assertThrows(UnauthorizedAccessException.class, () -> {
            kosService.updateKos(kosId, updatedKosData, anotherOwnerId); // Using different owner ID
        });

        // Verify find was called, but save was not
        verify(kosRepository, times(1)).findById(kosId);
        verify(kosRepository, never()).save(any(Kos.class));
    }

    @Test
    void deleteKos_Success() {
        // Arrange: Mock finding the existing kos
        when(kosRepository.findById(kosId)).thenReturn(Optional.of(kos));
        // Arrange: Mock the delete action (void method, so use doNothing)
        doNothing().when(kosRepository).deleteById(kosId); // Assuming deleteById is used

        // Act: Call the service method with the correct owner ID
        kosService.deleteKos(kosId, ownerId); // Assuming this method exists

        // Assert: Verify find and delete were called
        verify(kosRepository, times(1)).findById(kosId);
        verify(kosRepository, times(1)).deleteById(kosId);
    }

    @Test
    void deleteKos_NotFound() {
        // Arrange: Mock finding non-existent kos
        String nonExistentId = "non-existent-id";
        when(kosRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert: Check for KosNotFoundException
        assertThrows(KosNotFoundException.class, () -> {
            kosService.deleteKos(nonExistentId, ownerId);
        });

        // Verify find was called, but delete was not
        verify(kosRepository, times(1)).findById(nonExistentId);
        verify(kosRepository, never()).deleteById(anyString());
        verify(kosRepository, never()).delete(any(Kos.class)); // Check other potential delete methods too
    }

    @Test
    void deleteKos_Unauthorized() {
        // Arrange: Mock finding the existing kos (owned by 'owner')
        when(kosRepository.findById(kosId)).thenReturn(Optional.of(kos));

        // Act & Assert: Call the service method with the *wrong* owner ID and check for exception
        assertThrows(UnauthorizedAccessException.class, () -> {
            kosService.deleteKos(kosId, anotherOwnerId); // Using different owner ID
        });

        // Verify find was called, but delete was not
        verify(kosRepository, times(1)).findById(kosId);
        verify(kosRepository, never()).deleteById(anyString());
        verify(kosRepository, never()).delete(any(Kos.class));
    }

    @Test
    void searchKos_Success() {
        // Arrange: Prepare search keyword and expected results
        String keyword = "Test";
        List<Kos> expectedResults = List.of(kos);
        // Arrange: Mock repository search method
        when(kosRepository.searchByKeyword(keyword)).thenReturn(expectedResults); // Assuming this repo method exists

        // Act: Call the service search method
        List<Kos> actualResults = kosService.searchKos(keyword); // Assuming this service method exists

        // Assert: Verify the results
        assertNotNull(actualResults);
        assertEquals(expectedResults.size(), actualResults.size());
        assertEquals(expectedResults.get(0), actualResults.get(0));
        verify(kosRepository, times(1)).searchByKeyword(keyword);
    }

    @Test
    void searchKos_NoResults() {
        // Arrange: Prepare search keyword
        String keyword = "NotFound";
        // Arrange: Mock repository search method to return empty list
        when(kosRepository.searchByKeyword(keyword)).thenReturn(Collections.emptyList());

        // Act: Call the service search method
        List<Kos> actualResults = kosService.searchKos(keyword);

        // Assert: Verify the list is empty
        assertNotNull(actualResults);
        assertTrue(actualResults.isEmpty());
        verify(kosRepository, times(1)).searchByKeyword(keyword);
    }

    // Assuming getBasicKosInfoInternal is a method in KosService
    @Test
    void getBasicKosInfoInternal_Success() {
        // Arrange: Mock repository findById to return the kos object
        when(kosRepository.findById(kosId)).thenReturn(Optional.of(kos));

        // Act: Call the internal service method
        Kos resultKos = kosService.getBasicKosInfoInternal(kosId); // Assuming this method exists

        // Assert: Verify the correct object is returned (or basic checks)
        assertNotNull(resultKos);
        assertEquals(kosId, resultKos.getId());
        // Add more assertions if specific basic info is expected
        verify(kosRepository, times(1)).findById(kosId);
    }

    @Test
    void getBasicKosInfoInternal_NotFound() {
        // Arrange: Mock repository findById to return empty
        String nonExistentId = "non-existent-id";
        when(kosRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert: Check for KosNotFoundException
        assertThrows(KosNotFoundException.class, () -> {
            kosService.getBasicKosInfoInternal(nonExistentId);
        });

        // Verify findById was called
        verify(kosRepository, times(1)).findById(nonExistentId);
    }

     // --- Example: Basic findAllKos test based on current KosService ---
     @Test
     void findAllKos_ReturnsKosList() {
         List<Kos> expectedKosList = new ArrayList<>();
         expectedKosList.add(kos);
         when(kosRepository.findAll()).thenReturn(expectedKosList);

         List<Kos> actualKosList = kosService.findAllKos();

         assertEquals(expectedKosList.size(), actualKosList.size());
         assertEquals(expectedKosList.get(0), actualKosList.get(0));
         verify(kosRepository, times(1)).findAll();
     }

     // --- Example: Basic findAllPemilik test based on current KosService ---
     @Test
     void findAllPemilik_ReturnsPemilikList() {
         List<Pemilik> expectedPemilikList = new ArrayList<>();
         expectedPemilikList.add(owner);
         when(pemilikRepository.findAll()).thenReturn(expectedPemilikList);

         List<Pemilik> actualPemilikList = kosService.findAllPemilik();

         assertEquals(expectedPemilikList.size(), actualPemilikList.size());
         assertEquals(expectedPemilikList.get(0), actualPemilikList.get(0));
         verify(pemilikRepository, times(1)).findAll();
     }
}
