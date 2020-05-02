package ru.sovaowltv.exceptions.user;

import ru.sovaowltv.exceptions.JsonResponseException;

public class BadRulesException extends JsonResponseException {
    public BadRulesException(String message) {
        super(message);
    }
}
