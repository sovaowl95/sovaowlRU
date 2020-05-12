package ru.sovaowltv.exceptions.api.vk;

import ru.sovaowltv.exceptions.JsonResponseException;

public class VKGroupNotFound extends JsonResponseException {
    public VKGroupNotFound(String message) {
        super(message);
    }
}
