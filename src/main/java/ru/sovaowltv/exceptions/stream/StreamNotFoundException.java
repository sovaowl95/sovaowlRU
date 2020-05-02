package ru.sovaowltv.exceptions.stream;

import ru.sovaowltv.exceptions.JsonResponseException;

public class StreamNotFoundException extends JsonResponseException {
    public StreamNotFoundException(String message) {
        super(message);
    }
}
