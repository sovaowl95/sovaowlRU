package ru.sovaowltv.exceptions.user;

import ru.sovaowltv.exceptions.JsonResponseException;

public class LoginAlreadyInUseException extends JsonResponseException {
    public LoginAlreadyInUseException(String message) {
        super(message);
    }
}
