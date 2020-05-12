package ru.sovaowltv.exceptions;

public class BadParamsException extends JsonResponseException {
    public BadParamsException(String message) {
        super(message);
    }
}
