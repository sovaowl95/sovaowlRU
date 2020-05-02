package ru.sovaowltv.exceptions.user;

import ru.sovaowltv.exceptions.JsonResponseException;

public class UserGoogleRevokeFoundException extends JsonResponseException {
    public UserGoogleRevokeFoundException(String message) {
        super(message);
    }
}
