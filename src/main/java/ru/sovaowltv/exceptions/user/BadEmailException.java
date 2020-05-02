package ru.sovaowltv.exceptions.user;

import ru.sovaowltv.exceptions.JsonResponseException;

public class BadEmailException extends JsonResponseException {
    public BadEmailException(String message) {
        super(message);
    }
}
