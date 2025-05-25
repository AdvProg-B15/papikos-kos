package id.ac.ui.cs.advprog.papikos.kos.exception;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class KosNotFoundExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String errorMessage = "Kos entity was not found.";
        KosNotFoundException exception = new KosNotFoundException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String errorMessage = "Kos entity was not found with a specific cause.";
        Throwable cause = new IllegalArgumentException("Invalid ID format");
        KosNotFoundException exception = new KosNotFoundException(errorMessage, cause);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithKosId() {
        UUID kosId = UUID.randomUUID();
        String expectedMessage = "Kos with ID " + kosId + " not found";
        KosNotFoundException exception = new KosNotFoundException(kosId);
        assertEquals(expectedMessage, exception.getMessage());
        assertNull(exception.getCause());
    }
}

