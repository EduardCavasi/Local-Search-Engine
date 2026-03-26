package org.example.error;

/**
 * Custom exception thrown by initial connection to data source
 */
public class DatabaseException extends AppException {
  public DatabaseException(String message) {
    super(message);
  }
  public DatabaseException(String message, Throwable cause) {
    super(message, cause);
  }
}
