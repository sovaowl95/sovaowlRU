package ru.sovaowltv.service.chat.realization;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import ru.sovaowltv.model.apiauth.ApiUser;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.chat.MessageStatus;
import ru.sovaowltv.service.chat.ApiForChatLifeCycle;
import ru.sovaowltv.service.chat.ApiForChatModeration;
import ru.sovaowltv.service.io.IOExtractor;
import ru.sovaowltv.service.messages.MessageDeliver;
import ru.sovaowltv.service.messages.MessageValidator;
import ru.sovaowltv.service.messages.MessagesUtil;
import ru.sovaowltv.service.stream.StreamModerationUtil;
import ru.sovaowltv.service.stream.StreamUtil;
import ru.sovaowltv.service.styles.StyleUtil;
import ru.sovaowltv.service.time.TimeUtil;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Setter
@Slf4j
public abstract class ApiForChat extends Thread
        implements ApiForChatModeration, ApiForChatLifeCycle {
    protected String topicTarget;
    protected ApiUser apiUser;

    protected String ip;
    protected String port;

    protected String clientId;
    protected String clientSecret;
    protected String accessToken;
    protected String channelToConnect;

    protected WebSocketContainer container;
    protected Session session;

    protected boolean canWrite;
    protected boolean canRead;
    protected boolean work;

    private int timeToSleepOnException = 5;
    private long lastTimeException;

    @Value("${sovaowlRu}")
    protected String sovaowlRu;

    @Value("${sovaowlRuHttps}")
    protected String sovaowlRuHttps;

    @Autowired
    protected TimeUtil timeUtil;
    @Autowired
    protected StreamUtil streamUtil;
    @Autowired
    protected StyleUtil styleUtil;
    @Autowired
    protected StreamModerationUtil streamModerationUtil;
    @Autowired
    protected MessagesUtil messagesUtil;

    @Autowired
    protected IOExtractor ioExtractor;

    @Autowired
    protected ApiTimeouts apiTimeouts;
    @Autowired
    protected MessageValidator messageValidator;
    @Autowired
    protected MessageDeliver messageDeliver;
    @Autowired
    protected SimpMessagingTemplate template;

    private final ReentrantLock locker = new ReentrantLock();

    protected ApiForChat(String topicTarget,
                         String ip, String port, String clientId, String clientSecret,
                         String accessToken,
                         String channelToConnect, ApiUser apiUser,
                         boolean canRead, boolean canWrite) {
        this.topicTarget = topicTarget;
        this.ip = ip;
        this.port = port;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessToken = accessToken;
        this.channelToConnect = channelToConnect;
        this.apiUser = apiUser;
        this.canRead = canRead;
        this.canWrite = canWrite;
        work = true;
        container = ContainerProvider.getWebSocketContainer();
    }


    protected abstract boolean refreshToken();

    protected abstract String getConnectString();

    public abstract void sendMessage(Message message, boolean fromSelf);

    public abstract void sendInviteMessage(String nickName);

    /**
     * ApiForChatLifeCycle
     */
    @OnClose
    public final void onClose(Session session, CloseReason reason) {
        log.error("socket closed " + this.getClass().getSimpleName() + " " + apiUser.getNick() + " " + channelToConnect);
        log.error(reason.toString());
        if (work)
            connect();
    }

    @OnError
    public final void onError(Session session, Throwable t) {
        log.error("some socket error. trying reconnect " + this.getClass().getSimpleName() + " " + apiUser.getNick() + " " + channelToConnect, t);
        if (work)
            connect();
    }


    boolean connect() {
        log.info("connect");
        while (work) {
            try {
                if (!refreshToken()) timeUtil.sleepSeconds(5);
                if (session != null) {
                    closeSession();
                }
                timeUtil.sleepSeconds(1);
                session = container.connectToServer(this, new URI(getConnectString()));
                log.info("socket opened " + this.getClass().getSimpleName() + " nick:" + apiUser.getNick() + " channel:" + channelToConnect);
                timeUtil.sleepSeconds(1);
                lastTimeException = -1;
                timeToSleepOnException = 5;
                return true;
            } catch (Exception e) {
                log.error("socket deployment exception " + this.getClass().getSimpleName() + " nick:" + apiUser.getNick() + " channel:" + channelToConnect, e);
                if (System.currentTimeMillis() - lastTimeException <= timeToSleepOnException + 5000) {
                    timeToSleepOnException = timeToSleepOnException + 10;
                }
                lastTimeException = System.currentTimeMillis();
                timeUtil.sleepSeconds(timeToSleepOnException);
            }
        }
        closeSession();
        return false;
    }

    private void closeSession() {
        log.info("closeSession");
        try {
            session.close();
            session = null;
        } catch (Exception e) {
            log.warn("session close goes wrong", e);
        }
    }

    //todo: additional validation!
    //todo: ANOTHER API SERVICE
    final boolean isMessageFromSite(@NotNull String text, String source) {
        String regex = "";
        if (text.startsWith(sovaowlRu)) {
            regex = sovaowlRu;
        } else if (text.startsWith("***")) {
            regex = "***";
        } else {
            return false;
        }

        if (text.startsWith(regex + " [")) {
            int index = (regex + " [").length();
            int endIndex = index + 1;
            while (Character.isDigit(text.charAt(endIndex))) endIndex++;
            String id = text.substring(index, endIndex);
            if (source.equalsIgnoreCase("twitch")) {

            } else if (source.equalsIgnoreCase("gg")) {
                if (id.equalsIgnoreCase("0")) return true;
            } else if (source.equalsIgnoreCase("yt")) {
                if (id.equalsIgnoreCase("0")) return true;
            }
            Optional<Message> m1 = messagesUtil.getMessageOptionalById(id);
            Optional<MessageStatus> m2 = messagesUtil.getMessageStatusOptionalById(id);
            return m1.isPresent() || m2.isPresent();
        }
        return false;
    }

    boolean sendToServer(String text) {
        try {
            while (session == null) Thread.sleep(200);
            locker.lock();
            if (!session.isOpen()) {
                log.error("session is closed. " + text);
                return false;
            }
            session.getBasicRemote().sendText(text);
            session.getBasicRemote().flushBatch();
            log.info(">>>" + channelToConnect + " text: " + text);
            return true;
        } catch (Exception e) {
            log.error("some socket error. trying reconnect " + this.getClass().getSimpleName() + " " + apiUser.getNick() + " " + channelToConnect, e);
            return false;
        } finally {
            locker.unlock();
        }
    }

    boolean sendPongToServer(String text) {
        try {
            while (session == null) Thread.sleep(50);
            locker.lock();
            if (!session.isOpen()) {
                log.error("session is closed. " + text);
                return false;
            }
            session.getBasicRemote().sendText(text);
            session.getBasicRemote().flushBatch();
            return true;
        } catch (Exception e) {
            log.error("some socket error. trying reconnect " + this.getClass().getSimpleName() + " " + apiUser.getNick() + " " + channelToConnect, e);
            return false;
        } finally {
            locker.unlock();
        }
    }

    public void disconnect() {
        try {
            work = false;
            session.close();
        } catch (IOException e) {
            log.error("some socket error. trying reconnect " + this.getClass().getSimpleName() + " " + apiUser.getNick() + " " + channelToConnect, e);
        }
    }

    @Override
    public void run() {
        connect();
    }
}