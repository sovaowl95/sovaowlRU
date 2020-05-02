package ru.sovaowltv.exceptions.user;

import ru.sovaowltv.exceptions.JsonResponseException;

public class UserTwitchNotFoundException extends JsonResponseException {
    public UserTwitchNotFoundException(String message) {
        super(message);
    }
}
