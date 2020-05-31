package ru.sovaowltv.service.spammer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.sovaowltv.model.apinotification.NotificationFor;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.spammer.Spammer;
import ru.sovaowltv.model.spammer.SpammerStatus;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.messages.MessageApiDeliver;
import ru.sovaowltv.service.messages.MessagesUtil;
import ru.sovaowltv.service.time.TimeUtil;
import ru.sovaowltv.service.unclassified.KeyWordsReplacerUtil;

import java.time.LocalDateTime;

import static ru.sovaowltv.service.unclassified.Constants.SPAM;

@RequiredArgsConstructor
@Getter
@Setter
@Slf4j
class SpammerThread extends Thread {
    private final KeyWordsReplacerUtil keyWordsReplacerUtil;
    private final TimeUtil timeUtil;
    private final MessageApiDeliver messageApiDeliver;
    private final MessagesUtil messageUtil;
    private final Spammer spammer;

    void stopSpam() {
        this.interrupt();
    }

    @Override
    public void run() {
        timeUtil.sleepSeconds(spammer.getDelay());
        while (!this.isInterrupted() && spammer.getSpammerStatus() == SpammerStatus.RUN) {
            try {
                Stream stream = spammer.getStream();

                if (stream.isLive()) {
                    Message message = new Message();
                    message.setType(SPAM);

                    String text = spammer.getText();
                    text = keyWordsReplacerUtil.replaceAllKeyWords(stream, text, NotificationFor.WEBSITE_SPAMMER);
                    message.setText(text);
                    message.setOriginalMessage(text);

                    message.setTime(LocalDateTime.now());
                    User user = stream.getUser();

                    messageApiDeliver.sendMessageToAllApiChats(message, user.getNickname(), null, user, stream);
                    messageUtil.convertAndSend(user.getNickname(), message);
                }
                timeUtil.sleepSeconds(spammer.getTime());
            } catch (Exception e) {
                log.error("spammer thread error", e);
                stopSpam();
                return;
            }
        }
    }
}
