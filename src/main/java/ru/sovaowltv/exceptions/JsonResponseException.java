package ru.sovaowltv.exceptions;

public class JsonResponseException extends RuntimeException {
    public JsonResponseException() {
        super();
    }

    public JsonResponseException(String message) {
        super(message);
    }

    public JsonResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonResponseException(Throwable cause) {
        super(cause);
    }

    public JsonResponseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
