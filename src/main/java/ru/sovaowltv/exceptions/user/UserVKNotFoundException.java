package ru.sovaowltv.exceptions.user;

import ru.sovaowltv.exceptions.JsonResponseException;

public class UserVKNotFoundException extends JsonResponseException {
    public UserVKNotFoundException(String message) {
        super(message);
    }
}
