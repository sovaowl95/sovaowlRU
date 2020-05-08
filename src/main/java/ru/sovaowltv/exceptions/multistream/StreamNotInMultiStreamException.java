package ru.sovaowltv.exceptions.multistream;


import ru.sovaowltv.exceptions.JsonResponseException;

public class StreamNotInMultiStreamException extends JsonResponseException {
    public StreamNotInMultiStreamException(String s) {
        super(s);
    }
}
