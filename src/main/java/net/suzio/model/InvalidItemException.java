package net.suzio.model;

// needed by javadoc reference; no harm

/**
 * Exception raised when an Item cnnot be created
 *
 * @author Michael Suzio
 */
public class InvalidItemException extends Exception {
    public InvalidItemException() {
    }

    public InvalidItemException(String message) {
        super(message);
    }

    public InvalidItemException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidItemException(Throwable cause) {
        super(cause);
    }

    public InvalidItemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
