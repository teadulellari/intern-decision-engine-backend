package ee.taltech.inbankbackend.exception;

/**
 * Thrown when customer who requested loan, is either underage or in high risk group.
 */
public class InvalidAgeException extends Exception {
    public InvalidAgeException(String message) { super(message); }
}
