package id.ac.ui.cs.advprog.papikos.kos.exception;

/**
 * Exception thrown when a requested Kos entity cannot be found.
 * Typically used when searching for a Kos by its ID and it does not exist
 * in the repository.
 */
public class KosNotFoundException extends RuntimeException {

    /**
     * Constructs a new KosNotFoundException with the specified detail message.
     *
     * @param message the detail message.
     */
    public KosNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new KosNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause (which is saved for later retrieval by the getCause() method).
     *                (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public KosNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new KosNotFoundException for a specific Kos ID.
     *
     * @param kosId The ID of the Kos that was not found.
     */
    public KosNotFoundException(java.util.UUID kosId) {
        super("Kos with ID " + kosId + " not found");
    }
}
