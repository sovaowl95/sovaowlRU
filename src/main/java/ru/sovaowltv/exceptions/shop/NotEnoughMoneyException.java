package ru.sovaowltv.exceptions.shop;

import ru.sovaowltv.exceptions.JsonResponseException;

public class NotEnoughMoneyException extends JsonResponseException {
    public NotEnoughMoneyException(String message) {
        super(message);
    }
}
