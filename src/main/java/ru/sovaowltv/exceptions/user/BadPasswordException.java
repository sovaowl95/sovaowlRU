package ru.sovaowltv.exceptions.user;

import ru.sovaowltv.exceptions.JsonResponseException;

public class BadPasswordException extends JsonResponseException {
    public BadPasswordException(String message) {
        super(message);
    }
}
