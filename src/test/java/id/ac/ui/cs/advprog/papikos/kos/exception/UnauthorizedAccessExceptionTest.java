package id.ac.ui.cs.advprog.papikos.kos.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UnauthorizedAccessExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String errorMessage = "User is not authorized to perform this action.";
        UnauthorizedAccessException exception = new UnauthorizedAccessException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String errorMessage = "User is not authorized, underlying security issue.";
        Throwable cause = new SecurityException("Token expired");
        UnauthorizedAccessException exception = new UnauthorizedAccessException(errorMessage, cause);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}

