package id.ac.ui.cs.advprog.papikos.kos.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.UUID;

class RentalEventTest {

    @Test
    void testRentalEventData() {
        RentalEvent event = new RentalEvent();
        String userId = UUID.randomUUID().toString();
        String kosId = UUID.randomUUID().toString();
        String rentalId = UUID.randomUUID().toString();
        BigDecimal price = new BigDecimal("100.00");
        String timestamp = "2025-05-25T10:00:00Z";

        event.setUserId(userId);
        event.setKosId(kosId);
        event.setRentalId(rentalId);
        event.setPrice(price);
        event.setTimestamp(timestamp);

        assertEquals(userId, event.getUserId());
        assertEquals(kosId, event.getKosId());
        assertEquals(rentalId, event.getRentalId());
        assertEquals(price, event.getPrice());
        assertEquals(timestamp, event.getTimestamp());

        // Test toString, hashCode, equals if not covered by Lombok's @Data
        // For @Data, these are usually generated and covered implicitly
        // but adding a simple toString check can be useful.
        assertTrue(event.toString().contains(userId));
        assertTrue(event.toString().contains(kosId));

        RentalEvent event2 = new RentalEvent();
        event2.setUserId(userId);
        event2.setKosId(kosId);
        event2.setRentalId(rentalId);
        event2.setPrice(price);
        event2.setTimestamp(timestamp);

        assertEquals(event, event2);
        assertEquals(event.hashCode(), event2.hashCode());

        RentalEvent event3 = new RentalEvent();
        event3.setUserId(UUID.randomUUID().toString()); // Different userId
        event3.setKosId(kosId);
        event3.setRentalId(rentalId);
        event3.setPrice(price);
        event3.setTimestamp(timestamp);

        assertNotEquals(event, event3);
        assertNotEquals(event.hashCode(), event3.hashCode());
    }
}

