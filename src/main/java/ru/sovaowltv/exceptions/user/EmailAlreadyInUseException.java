package ru.sovaowltv.exceptions.user;

import ru.sovaowltv.exceptions.JsonResponseException;

public class EmailAlreadyInUseException extends JsonResponseException {
    public EmailAlreadyInUseException(String message) {
        super(message);
    }
}
