package org.fbi.linking.codec.dataformat.format;

public class FormatException extends Exception {

    private static final long serialVersionUID = 1L;

    public FormatException() {
    }

    public FormatException(String message) {
        super(message);
    }

    public FormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormatException(Throwable cause) {
        super(cause);
    }

}
