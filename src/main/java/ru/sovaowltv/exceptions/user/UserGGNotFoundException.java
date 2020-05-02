package ru.sovaowltv.exceptions.user;

import ru.sovaowltv.exceptions.JsonResponseException;

public class UserGGNotFoundException extends JsonResponseException {
    public UserGGNotFoundException(String message) {
        super(message);
    }
}
