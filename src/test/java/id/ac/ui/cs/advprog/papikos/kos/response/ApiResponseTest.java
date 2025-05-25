package id.ac.ui.cs.advprog.papikos.kos.response;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testApiResponseBuilder_Success() {
        String data = "Test Data";
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK)
                .message("Operation successful")
                .data(data)
                .build();

        assertEquals(200, response.getStatus());
        assertEquals("Operation successful", response.getMessage());
        assertEquals(data, response.getData());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    void testApiResponseBuilder_DefaultStatus() {
        ApiResponse<Object> response = ApiResponse.builder()
                .message("Default status test")
                .build();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Default status test", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testConvenienceMethod_ok() {
        String data = "OK Data";
        ApiResponse<String> response = ApiResponse.<String>builder().ok(data);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Success", response.getMessage());
        assertEquals(data, response.getData());
    }

    @Test
    void testConvenienceMethod_created() {
        Integer data = 123;
        ApiResponse<Integer> response = ApiResponse.<Integer>builder().created(data);

        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals("Resource created successfully", response.getMessage());
        assertEquals(data, response.getData());
    }

    @Test
    void testConvenienceMethod_badRequest() {
        String errorMessage = "Invalid input";
        ApiResponse<?> response = ApiResponse.builder().badRequest(errorMessage);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals(errorMessage, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testConvenienceMethod_notFound() {
        String errorMessage = "Resource not found";
        ApiResponse<?> response = ApiResponse.builder().notFound(errorMessage);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals(errorMessage, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testConvenienceMethod_internalError() {
        String errorMessage = "Server error occurred";
        ApiResponse<?> response = ApiResponse.builder().internalError(errorMessage);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        assertEquals(errorMessage, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testApiResponse_NullData() {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("No content")
                .build();

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
        assertEquals("No content", response.getMessage());
        assertNull(response.getData());
    }
}

