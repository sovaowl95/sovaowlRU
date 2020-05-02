package ru.sovaowltv.exceptions.stream;

import ru.sovaowltv.exceptions.JsonResponseException;

public class NotYourStreamException extends JsonResponseException {
    public NotYourStreamException(String message) {
        super(message);
    }
}
