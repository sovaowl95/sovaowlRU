package ru.sovaowltv.service.spammer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.spammer.Spammer;
import ru.sovaowltv.model.spammer.SpammerStatus;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.repositories.stream.SpammerRepository;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpammerFactory {
    private final StreamRepositoryHandler streamRepositoryHandler;
    private final SpammerRepository spammerRepository;

    Spammer createNewSpammer(Stream stream) {
        Spammer spammer = new Spammer();
        spammer.setStream(stream);
        spammer.setText("default spammer text");
        spammer.setTime(60);
        spammer.setDelay(0);
        spammer.setSpammerStatus(SpammerStatus.STOP);
        addSpammerToStream(stream, spammer);

        spammerRepository.save(spammer);
        streamRepositoryHandler.save(stream);
        return spammer;
    }

    private void addSpammerToStream(Stream stream, Spammer spammer) {
        Set<Spammer> spammerSet = stream.getSpammerSet();
        if (spammerSet == null) spammerSet = new HashSet<>();
        spammerSet.add(spammer);
    }
}
