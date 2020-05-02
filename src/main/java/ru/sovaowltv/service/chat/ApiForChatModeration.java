package ru.sovaowltv.service.chat;

import ru.sovaowltv.model.chat.Message;

public interface ApiForChatModeration {
    void timeoutUser(String nick, String time, String reason, Message message);

    void unTimeoutUser(String nick, Message message);

    void banUser(String nickName, String reason, Message message);

    void unBanUser(String nickName, Message message);

    void deleteMessage(Message message);

    void purgeUser(String nickName, Message message);
}
