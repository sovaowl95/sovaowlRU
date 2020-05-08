package ru.sovaowltv.exceptions.multistream;


import ru.sovaowltv.exceptions.JsonResponseException;

public class IncorrectJoinCodeException extends JsonResponseException {
    public IncorrectJoinCodeException(String s) {
        super(s);
    }
}
