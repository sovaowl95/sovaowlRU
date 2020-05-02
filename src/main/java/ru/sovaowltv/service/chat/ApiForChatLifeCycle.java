package ru.sovaowltv.service.chat;

import javax.websocket.*;
import java.io.IOException;

public interface ApiForChatLifeCycle {
    @OnOpen
    void onOpen(Session session) throws IOException;

    @OnMessage
    void onMessage(Session session, String message);

    @OnClose
    void onClose(Session session, CloseReason reason);

    @OnError
    void onError(Session session, Throwable t);
}
