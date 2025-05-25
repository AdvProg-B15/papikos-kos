package id.ac.ui.cs.advprog.papikos.kos.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VerifyTokenResponseTest {

    @Test
    void testVerifyTokenResponseData() {
        VerifyTokenResponse.Data data = VerifyTokenResponse.Data.builder()
                .userId("user123")
                .email("test@example.com")
                .role("USER")
                .status("ACTIVE")
                .build();

        VerifyTokenResponse response = VerifyTokenResponse.builder()
                .status(200)
                .message("Success")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();

        assertEquals(200, response.getStatus());
        assertEquals("Success", response.getMessage());
        assertEquals(data, response.getData());
        assertTrue(response.getTimestamp() > 0);

        assertEquals("user123", data.getUserId());
        assertEquals("test@example.com", data.getEmail());
        assertEquals("USER", data.getRole());
        assertEquals("ACTIVE", data.getStatus());

        // Test setters and getters not covered by builder
        VerifyTokenResponse responseNoArgs = new VerifyTokenResponse();
        responseNoArgs.setStatus(400);
        responseNoArgs.setMessage("Error");
        responseNoArgs.setData(null);
        responseNoArgs.setTimestamp(12345L);

        assertEquals(400, responseNoArgs.getStatus());
        assertEquals("Error", responseNoArgs.getMessage());
        assertNull(responseNoArgs.getData());
        assertEquals(12345L, responseNoArgs.getTimestamp());

        VerifyTokenResponse.Data dataNoArgs = new VerifyTokenResponse.Data();
        dataNoArgs.setUserId("user456");
        dataNoArgs.setEmail("another@example.com");
        dataNoArgs.setRole("ADMIN");
        dataNoArgs.setStatus("INACTIVE");

        assertEquals("user456", dataNoArgs.getUserId());
        assertEquals("another@example.com", dataNoArgs.getEmail());
        assertEquals("ADMIN", dataNoArgs.getRole());
        assertEquals("INACTIVE", dataNoArgs.getStatus());

        // Test toString, hashCode, equals for @Data or @Builder + @AllArgsConstructor + @NoArgsConstructor
        VerifyTokenResponse.Data data2 = VerifyTokenResponse.Data.builder()
                .userId("user123")
                .email("test@example.com")
                .role("USER")
                .status("ACTIVE")
                .build();
        assertEquals(data, data2);
        assertEquals(data.hashCode(), data2.hashCode());
        assertTrue(data.toString().contains("user123"));

        VerifyTokenResponse response2 = VerifyTokenResponse.builder()
                .status(200)
                .message("Success")
                .data(data2) // use data2 which is equal to data
                .timestamp(response.getTimestamp()) // use same timestamp for equality
                .build();
        assertEquals(response, response2);
        assertEquals(response.hashCode(), response2.hashCode());
        assertTrue(response.toString().contains("Success"));
    }
}

