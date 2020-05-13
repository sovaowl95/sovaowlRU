package ru.sovaowltv.service.spammer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sovaowltv.model.apinotification.NotificationFor;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.spammer.Spammer;
import ru.sovaowltv.model.spammer.SpammerStatus;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.stream.SpammerRepository;
import ru.sovaowltv.service.messages.MessageApiDeliver;
import ru.sovaowltv.service.messages.MessagesUtil;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;
import ru.sovaowltv.service.time.TimeUtil;
import ru.sovaowltv.service.unclassified.KeyWordsReplacerUtil;

import java.time.LocalDateTime;

import static ru.sovaowltv.service.unclassified.Constants.SPAM;

@RequiredArgsConstructor
@Getter
@Setter
@Slf4j
class SpammerThread extends Thread {
    @Autowired
    private SpammerRepository spammerRepository;
    @Autowired
    private StreamRepositoryHandler streamRepositoryHandler;

    @Autowired
    private KeyWordsReplacerUtil keyWordsReplacerUtil;
    @Autowired
    private TimeUtil timeUtil;

    @Autowired
    private MessageApiDeliver messageApiDeliver;

    @Autowired
    private MessagesUtil messageUtil;

    private Spammer spammer;
    private boolean work = true;

    void stopSpam() {
        spammer.setSpammerStatus(SpammerStatus.STOP);
        spammerRepository.save(spammer);
        work = false;
        this.interrupt();
    }

    @Override
    public void run() {
        spammer.setSpammerStatus(SpammerStatus.RUN);
        spammerRepository.save(spammer);
        timeUtil.sleepSeconds(spammer.getDelay());
        while (work && spammer.getSpammerStatus() == SpammerStatus.RUN) {
            try {
                Stream stream = streamRepositoryHandler.getStreamById(spammer.getStream().getId());

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
                    messageUtil.convertAndSend("/topic/" + user.getNickname(), message);
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
