package ru.sovaowltv.exceptions.multistream;


import ru.sovaowltv.exceptions.JsonResponseException;

public class StreamAlreadyInMultiStreamException extends JsonResponseException {
    public StreamAlreadyInMultiStreamException(String s) {
        super(s);
    }
}
