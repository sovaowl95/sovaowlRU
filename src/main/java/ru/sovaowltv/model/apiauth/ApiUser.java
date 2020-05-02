package ru.sovaowltv.model.apiauth;

import ru.sovaowltv.model.user.User;

import java.io.Serializable;
import java.time.LocalDateTime;

public interface ApiUser extends Serializable {
    long getId();

    void setId(long id);

    String getSub();

    void setSub(String sub);

    User getUser();

    void setUser(User user);

    String getNick();

    void setNick(String nick);

    String getAccessToken();

    void setAccessToken(String accessToken);

    String getRefreshToken();

    void setRefreshToken(String refreshToken);

    LocalDateTime getExpiresIn();

    void setExpiresIn(LocalDateTime expiresIn);

    boolean isExpired();

    void setExpired(boolean corrupted);

    boolean isCorrupted();

    void setCorrupted(boolean corrupted);
}
