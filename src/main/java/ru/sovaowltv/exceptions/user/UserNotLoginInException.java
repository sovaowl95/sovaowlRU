package ru.sovaowltv.exceptions.user;

import ru.sovaowltv.exceptions.JsonResponseException;

public class UserNotLoginInException extends JsonResponseException {
    public UserNotLoginInException(String message) {
        super(message);
    }
}
