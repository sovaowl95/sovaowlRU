package ru.sovaowltv.exceptions.multistream;


import ru.sovaowltv.exceptions.JsonResponseException;

public class MultiStreamAlreadyExistsException extends JsonResponseException {
    public MultiStreamAlreadyExistsException(String s) {
        super(s);
    }
}
