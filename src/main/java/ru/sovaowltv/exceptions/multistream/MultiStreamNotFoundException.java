package ru.sovaowltv.exceptions.multistream;


import ru.sovaowltv.exceptions.JsonResponseException;

public class MultiStreamNotFoundException extends JsonResponseException {
    public MultiStreamNotFoundException(String s) {
        super(s);
    }
}
