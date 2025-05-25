package id.ac.ui.cs.advprog.papikos.kos.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class KosTest {

    private Kos kos1;
    private Kos kos2;
    private UUID id1;
    private UUID ownerId1;

    @BeforeEach
    void setUp() {
        id1 = UUID.randomUUID();
        ownerId1 = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        kos1 = new Kos();
        kos1.setId(id1);
        kos1.setOwnerUserId(ownerId1);
        kos1.setName("Kos Test 1");
        kos1.setAddress("Jl. Test 1");
        kos1.setDescription("Description 1");
        kos1.setNumRooms(10);
        kos1.setMonthlyRentPrice(new BigDecimal("1000000"));
        kos1.setIsListed(true);
        kos1.setCreatedAt(now);
        kos1.setUpdatedAt(now);

        kos2 = new Kos(id1, ownerId1, "Kos Test 1", "Jl. Test 1", "Description 1", 10, new BigDecimal("1000000"), true, now, now);
    }

    @Test
    void testNoArgsConstructor() {
        Kos kos = new Kos();
        assertNull(kos.getId());
        assertNull(kos.getOwnerUserId());
        assertNull(kos.getName());
        assertTrue(kos.getIsListed()); // Default value
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        String name = "Kos Lengkap";
        String address = "Jl. Lengkap";
        String description = "Deskripsi Lengkap";
        Integer numRooms = 5;
        BigDecimal price = new BigDecimal("500000");
        Boolean isListed = false;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        Kos kos = new Kos(id, ownerId, name, address, description, numRooms, price, isListed, createdAt, updatedAt);

        assertEquals(id, kos.getId());
        assertEquals(ownerId, kos.getOwnerUserId());
        assertEquals(name, kos.getName());
        assertEquals(address, kos.getAddress());
        assertEquals(description, kos.getDescription());
        assertEquals(numRooms, kos.getNumRooms());
        assertEquals(price, kos.getMonthlyRentPrice());
        assertEquals(isListed, kos.getIsListed());
        assertEquals(createdAt, kos.getCreatedAt());
        assertEquals(updatedAt, kos.getUpdatedAt());
    }

    @Test
    void testSetters() {
        Kos kos = new Kos();
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        String name = "Kos Setter";
        // ... (set all other fields)

        kos.setId(id);
        kos.setOwnerUserId(ownerId);
        kos.setName(name);
        // ...

        assertEquals(id, kos.getId());
        assertEquals(ownerId, kos.getOwnerUserId());
        assertEquals(name, kos.getName());
        // ... (assert all other fields)
    }

    @Test
    void testPrePersist() {
        Kos kos = new Kos();
        assertNull(kos.getCreatedAt());
        assertNull(kos.getUpdatedAt());

        kos.onCreate(); // Manually call for testing

        assertNotNull(kos.getCreatedAt());
        assertNotNull(kos.getUpdatedAt());
    }

    @Test
    void testPreUpdate() {
        Kos kos = new Kos();
        kos.onCreate(); // Set initial times
        LocalDateTime initialCreatedAt = kos.getCreatedAt();
        LocalDateTime initialUpdatedAt = kos.getUpdatedAt();

        try {
            // Introduce a slight delay to ensure updatedAt changes
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        kos.onUpdate(); // Manually call for testing

        assertNotNull(kos.getUpdatedAt());
        assertEquals(initialCreatedAt, kos.getCreatedAt()); // CreatedAt should not change
        assertTrue(kos.getUpdatedAt().isAfter(initialUpdatedAt));
    }

    @Test
    void testEqualsAndHashCode() {
        // Reflexivity
        assertEquals(kos1, kos1);
        assertEquals(kos1.hashCode(), kos1.hashCode());

        // Symmetry
        // kos2 is constructed to be equal to kos1 based on fields in equals method
        assertEquals(kos1, kos2);
        assertEquals(kos2, kos1);
        assertEquals(kos1.hashCode(), kos2.hashCode());

        // Different ID
        Kos kos3 = new Kos(UUID.randomUUID(), ownerId1, "Kos Test 1", "Jl. Test 1", "Description 1", 10, new BigDecimal("1000000"), true, kos1.getCreatedAt(), kos1.getUpdatedAt());
        assertNotEquals(kos1, kos3);
        assertNotEquals(kos1.hashCode(), kos3.hashCode()); // HashCode might collide, but unlikely for UUIDs

        // Different Name
        Kos kos4 = new Kos(id1, ownerId1, "Kos Test Different Name", "Jl. Test 1", "Description 1", 10, new BigDecimal("1000000"), true, kos1.getCreatedAt(), kos1.getUpdatedAt());
        assertNotEquals(kos1, kos4);
        assertNotEquals(kos1.hashCode(), kos4.hashCode());

        // Null object
        assertNotEquals(kos1, null);

        // Different class
        assertNotEquals(kos1, new Object());

        // Test with all fields different for hashCode coverage
        Kos kosAllDifferent = new Kos(UUID.randomUUID(), UUID.randomUUID(), "Diff Name", "Diff Address", "Diff Desc", 20, BigDecimal.TEN, false, LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(4));
        assertNotEquals(kos1.hashCode(), kosAllDifferent.hashCode());

    }

    @Test
    void testToString() {
        String kosToString = kos1.toString();
        assertTrue(kosToString.contains(kos1.getId().toString()));
        assertTrue(kosToString.contains(kos1.getName()));
        assertTrue(kosToString.contains(kos1.getAddress()));
    }
}

