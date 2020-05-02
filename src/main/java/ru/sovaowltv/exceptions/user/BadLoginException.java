package ru.sovaowltv.exceptions.user;

import ru.sovaowltv.exceptions.JsonResponseException;

public class BadLoginException extends JsonResponseException {
    public BadLoginException(String message) {
        super(message);
    }
}
