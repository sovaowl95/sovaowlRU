package ru.sovaowltv.exceptions.user;

import ru.sovaowltv.exceptions.JsonResponseException;

public class BadGenderException extends JsonResponseException {
    public BadGenderException(String message) {
        super(message);
    }
}
