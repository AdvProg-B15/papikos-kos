package id.ac.ui.cs.advprog.papikos.kos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Or another appropriate status
public class InvalidOwnerException extends RuntimeException {
  public InvalidOwnerException(String message) {
    super(message);
  }

  public InvalidOwnerException(String message, Throwable cause) {
    super(message, cause);
  }
}
