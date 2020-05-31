package ru.sovaowltv.service.spammer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.exceptions.stream.NotYourStreamException;
import ru.sovaowltv.model.spammer.Spammer;
import ru.sovaowltv.model.spammer.SpammerStatus;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.repositories.stream.SpammerRepository;
import ru.sovaowltv.service.messages.MessageApiDeliver;
import ru.sovaowltv.service.messages.MessagesUtil;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;
import ru.sovaowltv.service.time.TimeUtil;
import ru.sovaowltv.service.unclassified.KeyWordsReplacerUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpammerUtil {
    private final SpammerRepository spammerRepository;
    private final StreamRepositoryHandler streamRepositoryHandler;

    private final SpammerFactory spammerFactory;

    //for inject
    private final KeyWordsReplacerUtil keyWordsReplacerUtil;
    private final TimeUtil timeUtil;
    private final MessageApiDeliver messageApiDeliver;
    private final MessagesUtil messageUtil;

    final Map<Long, SpammerThread> spammerMap = new HashMap<>();

    public void createSpam(Stream stream) {
        Spammer newSpammer = spammerFactory.createNewSpammer(stream);
        addSpammer(newSpammer);
    }

    private void addSpammer(Spammer spammer) {
        if (!spammerMap.containsKey(spammer.getId())) {
            addSpammerInMap(spammer);
        } else {
            log.warn("duplicate adding spammer: {}", spammer.getId());
        }
    }

    SpammerThread addSpammerInMap(Spammer spammer) {
        SpammerThread spammerThread
                = new SpammerThread(keyWordsReplacerUtil, timeUtil, messageApiDeliver, messageUtil, spammer);
        spammerMap.put(spammer.getId(), spammerThread);
        return spammerThread;
    }


    public void deleteSpam(Stream stream, long id) {
        Spammer yourSpammer = isYourSpammer(stream, id);
        stream.getSpammerSet().remove(yourSpammer);
        streamRepositoryHandler.save(stream);

        stopSpammerIfExists(id);

        spammerRepository.deleteById(id);
    }


    public void startSpammer(Stream stream, long id) {
        Spammer spammer = isYourSpammer(stream, id);
        stopSpammerIfExists(id);

        SpammerThread spammerThread = addSpammerInMap(spammer);
        spammerThread.start();

        spammer.setSpammerStatus(SpammerStatus.RUN);
        spammerRepository.save(spammer);
        streamRepositoryHandler.save(stream);
    }


    public void stopSpammer(Stream stream, long id) {
        Spammer spammer = isYourSpammer(stream, id);
        stopSpammerIfExists(id);

        spammer.setSpammerStatus(SpammerStatus.STOP);
        spammerRepository.save(spammer);
        streamRepositoryHandler.save(stream);
    }

    public Spammer isYourSpammer(Stream stream, long id) {
        Optional<Spammer> first = stream.getSpammerSet().stream()
                .filter(spammer -> spammer.getId() == id)
                .findFirst();

        if (first.isEmpty()) {
            throw new NotYourStreamException("not your spammer");
        }

        return first.get();
    }


    private void stopSpammerIfExists(long id) {
        if (spammerMap.containsKey(id)) {
            spammerMap.get(id).stopSpam();
            spammerMap.remove(id);
        }
    }

    public void launchSpammers(Stream stream) {
        try {
            stream.getSpammerSet().forEach(spammer -> {
                try {
                    if (spammer.getSpammerStatus() == SpammerStatus.RUN) {
                        addSpammer(spammer);
                        startSpammer(stream, spammer.getId());
                    }
                } catch (Exception e) {
                    log.error("SpammerUtil launchSpammers -> ", e);
                }
            });
        } catch (Exception e) {
            log.error("error launch spammers for user {} {}", stream.getUser().getNickname(), e);
        }
    }
}