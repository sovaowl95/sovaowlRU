package ru.sovaowltv.exceptions.user;

import ru.sovaowltv.exceptions.JsonResponseException;


public class UserNotFoundException extends JsonResponseException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
