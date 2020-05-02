package ru.sovaowltv.exceptions.user;

import ru.sovaowltv.exceptions.JsonResponseException;

public class UserGoogleNotFoundException extends JsonResponseException {
    public UserGoogleNotFoundException(String message) {
        super(message);
    }
}
