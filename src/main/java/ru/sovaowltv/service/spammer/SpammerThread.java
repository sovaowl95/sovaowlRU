package ru.sovaowltv.service.spammer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import ru.sovaowltv.model.apinotification.NotificationFor;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.spammer.Spammer;
import ru.sovaowltv.model.spammer.SpammerStatus;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.stream.SpammerRepository;
import ru.sovaowltv.repositories.stream.StreamRepository;
import ru.sovaowltv.service.messages.MessageDeliver;
import ru.sovaowltv.service.time.TimeUtil;
import ru.sovaowltv.service.unclassified.KeyWordsReplacerUtil;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
@Setter
@Slf4j
class SpammerThread extends Thread {
    @Autowired
    private SpammerRepository spammerRepository;
    @Autowired
    private StreamRepository streamRepository;

    @Autowired
    private KeyWordsReplacerUtil keyWordsReplacerUtil;
    @Autowired
    private TimeUtil timeUtil;

    @Autowired
    private MessageDeliver messageDeliver;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private Spammer spammer;
    private boolean work;

    void stopSpam() {
        spammer.setSpammerStatus(SpammerStatus.STOP);
        spammerRepository.save(spammer);
        work = false;
        this.interrupt();
    }

    @Override
    public void run() {
        spammer.setSpammerStatus(SpammerStatus.RUN);
        work = true;
        spammerRepository.save(spammer);
        timeUtil.sleepSeconds(spammer.getDelay());
        while (work && spammer.getSpammerStatus() == SpammerStatus.RUN) {
            try {
                Optional<Stream> streamById = streamRepository.findById(spammer.getStream().getId());
                if (streamById.isPresent()) {
                    spammer.setStream(streamById.get());
                } else {
                    stopSpam();
                    return;
                }
                Message message = new Message();
                message.setType("spam");

                String text = spammer.getText();
                text = keyWordsReplacerUtil.replaceAllKeyWords(spammer.getStream(), text, NotificationFor.WEBSITE_SPAMMER);
                message.setText(text);
                message.setOriginalMessage(text);

                message.setTime(LocalDateTime.now());
                User user = spammer.getStream().getUser();
                messageDeliver.sendMessageToAllApiChats(message, user.getNickname(), null, user, streamById.get());
                simpMessagingTemplate.convertAndSend("/topic/" + user.getNickname(), message);

                timeUtil.sleepSeconds(spammer.getTime());
            } catch (Exception e) {
                log.error("spammer thread error", e);
            }
        }
    }
}
