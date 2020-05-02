package ru.sovaowltv.exceptions;

public class EmailSendingException extends JsonResponseException {
    public EmailSendingException(String message) {
        super(message);
    }
}
