package org.example.error;

/**
 * Custom exception for parse errors of the search request string
 */
public class SearchRequestParseException extends AppException {
    public SearchRequestParseException(String message) {
        super(message);
    }
    public SearchRequestParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
