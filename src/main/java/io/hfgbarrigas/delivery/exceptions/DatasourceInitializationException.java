package io.hfgbarrigas.delivery.exceptions;

public class DatasourceInitializationException extends RuntimeException {
    public DatasourceInitializationException() {
        super();
    }

    public DatasourceInitializationException(String message) {
        super(message);
    }

    public DatasourceInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatasourceInitializationException(Throwable cause) {
        super(cause);
    }

    protected DatasourceInitializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
