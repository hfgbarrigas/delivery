package io.hfgbarrigas.delivery.exceptions;

public class DuplicateAuthorityException extends RuntimeException {
    public DuplicateAuthorityException() {
    }

    public DuplicateAuthorityException(String message) {
        super(message);
    }

    public DuplicateAuthorityException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateAuthorityException(Throwable cause) {
        super(cause);
    }

    public DuplicateAuthorityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
