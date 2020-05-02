package ru.sovaowltv.exceptions.user;

import ru.sovaowltv.exceptions.JsonResponseException;
import ru.sovaowltv.model.apiauth.ApiUser;

public class UserApiCorruptedException extends JsonResponseException {
    public UserApiCorruptedException(ApiUser apiUser) {
        super(apiUser.getClass() + " " + apiUser.getNick());
    }
}
