package ru.sovaowltv.exceptions.chat;

import ru.sovaowltv.exceptions.JsonResponseException;

public class BadSmileServiceParamsException extends JsonResponseException {
    public BadSmileServiceParamsException(String message) {
        super(message);
    }
}
