package id.ac.ui.cs.advprog.papikos.kos.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvalidOwnerExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String errorMessage = "Invalid owner for this operation.";
        InvalidOwnerException exception = new InvalidOwnerException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String errorMessage = "Invalid owner for this operation with cause.";
        Throwable cause = new RuntimeException("Root cause");
        InvalidOwnerException exception = new InvalidOwnerException(errorMessage, cause);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}

