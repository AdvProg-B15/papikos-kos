package id.ac.ui.cs.advprog.papikos.kos.exception;

/**
 * Exception thrown when a user attempts to perform an action
 * for which they do not have the necessary authorization.
 * For example, trying to modify or delete a Kos entity they do not own.
 */
public class UnauthorizedAccessException extends RuntimeException {

    /**
     * Constructs a new UnauthorizedAccessException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the lack of authorization.
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    /**
     * Constructs a new UnauthorizedAccessException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause (which is saved for later retrieval by the getCause() method).
     *                (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}