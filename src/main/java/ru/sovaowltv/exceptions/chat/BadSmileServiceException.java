package ru.sovaowltv.exceptions.chat;

import ru.sovaowltv.exceptions.JsonResponseException;

public class BadSmileServiceException extends JsonResponseException {
    public BadSmileServiceException(String message) {
        super(message);
    }
}
