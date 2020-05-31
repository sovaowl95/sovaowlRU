package ru.sovaowltv.service.spammer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.spammer.Spammer;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.repositories.stream.SpammerRepository;
import ru.sovaowltv.service.unclassified.HtmlTagsClear;

@Service
@RequiredArgsConstructor
public class SpammerSettingsUtil {
    private final SpammerRepository spammerRepository;

    private final SpammerUtil spammerUtil;

    private final HtmlTagsClear htmlTagsClear;

    public void editSpamText(Stream stream, long id, String text) {
        Spammer spammer = spammerUtil.isYourSpammer(stream, id);
        spammer.setText(htmlTagsClear.removeTags(text));

        SpammerThread currentSpammer = spammerUtil.spammerMap.get(id);
        if (currentSpammer != null)
            currentSpammer.getSpammer().setText(spammer.getText());

        spammerRepository.save(spammer);
    }

    public void editSpamTime(Stream stream, long id, String time) {
        Spammer spammer = spammerUtil.isYourSpammer(stream, id);

        int timeLong = Integer.parseInt(time);
        if (timeLong < 60) timeLong = 60;
        spammer.setTime(timeLong);

        SpammerThread currentSpammer = spammerUtil.spammerMap.get(id);
        if (currentSpammer != null)
            currentSpammer.getSpammer().setTime(spammer.getTime());

        spammerRepository.save(spammer);
    }

    public void editSpamDelay(Stream stream, long id, String time) {
        Spammer spammer = spammerUtil.isYourSpammer(stream, id);

        int timeLong = Integer.parseInt(time);
        if (timeLong < 60) timeLong = 60;
        spammer.setTime(timeLong);

        SpammerThread currentSpammer = spammerUtil.spammerMap.get(id);
        if (currentSpammer != null)
            currentSpammer.getSpammer().setDelay(spammer.getTime());

        spammerRepository.save(spammer);
    }
}
