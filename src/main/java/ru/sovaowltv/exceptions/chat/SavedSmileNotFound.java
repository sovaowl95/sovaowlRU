package ru.sovaowltv.exceptions.chat;

import ru.sovaowltv.exceptions.JsonResponseException;

public class SavedSmileNotFound extends JsonResponseException {
    public SavedSmileNotFound(String message) {
        super(message);
    }
}
