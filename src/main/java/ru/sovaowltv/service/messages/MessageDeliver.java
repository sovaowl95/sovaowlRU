package ru.sovaowltv.service.messages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@RequiredArgsConstructor
@Slf4j
//todo: ANOTHER API SERVICE
public class MessageDeliver {
    private final MessageRunnable messageRunnable;

    private final ThreadPoolExecutor threadPoolExecutor = ((ThreadPoolExecutor) Executors.newCachedThreadPool());

    @PostConstruct
    public void init() {
        threadPoolExecutor.setMaximumPoolSize(500);
    }

    public void sendMessageToAllApiChats(Message message, String webSiteChannel, Object apiChatObject, User user, @Nullable Stream stream) {
        if (stream == null || stream.getId() > 0) {
            threadPoolExecutor.execute(messageRunnable.getRunnableSendToTwitch(message, webSiteChannel, apiChatObject, user));
            threadPoolExecutor.execute(messageRunnable.getRunnableSendToGG(message, webSiteChannel, apiChatObject, user));
            threadPoolExecutor.execute(messageRunnable.getRunnableSendToYT(message, webSiteChannel, apiChatObject, user));
        }
    }
}