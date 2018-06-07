package io.hfgbarrigas.delivery.exceptions;

public class DuplicatePlaceException extends RuntimeException {
    public DuplicatePlaceException() {
    }

    public DuplicatePlaceException(String message) {
        super(message);
    }

    public DuplicatePlaceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatePlaceException(Throwable cause) {
        super(cause);
    }

    public DuplicatePlaceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
