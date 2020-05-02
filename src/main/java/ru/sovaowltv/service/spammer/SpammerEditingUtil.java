package ru.sovaowltv.service.spammer;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.spammer.Spammer;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.stream.SpammerRepository;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.unclassified.HtmlTagsClear;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

@Service
@RequiredArgsConstructor
public class SpammerEditingUtil {
    private final SpammerRepository spammerRepository;
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final UserUtil userUtil;
    private final SpammerUtil spammerUtil;

    private final DataExtractor dataExtractor;
    private final HtmlTagsClear htmlTagsClear;

    private void editTextSpammer(Spammer spammer) {
        long id = spammer.getId();
        if (!spammerUtil.spammerMap.containsKey(id)) {
            spammerUtil.addSpammerInMap(spammer, id);
        }
        SpammerThread currentSpammer = spammerUtil.spammerMap.get(id);
        Spammer spammerInsideThread = currentSpammer.getSpammer();
        spammerInsideThread.setText(spammer.getText());
    }

    public void editSpamText(String json) {
        JsonObject jsonObject = dataExtractor.extractJsonFromString(json);
        String id = dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "id");
        String text = dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "text");

        User user = userUtil.getUser();
        Spammer spammer = spammerUtil.getSpammerByIdAndVerifyByUser(id, user);
        usersRepositoryHandler.free(user);

        spammer.setText(htmlTagsClear.removeTags(text));
        spammerRepository.save(spammer);
        editTextSpammer(spammer);
    }

    public void editSpamTime(String json) {
        JsonObject jsonObject = dataExtractor.extractJsonFromString(json);
        String id = dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "id");
        String time = dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "time");

        User user = userUtil.getUser();
        Spammer spammer = spammerUtil.getSpammerByIdAndVerifyByUser(id, user);
        usersRepositoryHandler.free(user);

        int timeLong = Integer.parseInt(htmlTagsClear.removeTags(time));
        if (timeLong < 60) timeLong = 60;
        spammer.setTime(timeLong);
        spammerRepository.save(spammer);
        editTimeSpammer(spammer);
    }

    private void editTimeSpammer(Spammer spammer) {
        long id = spammer.getId();
        if (!spammerUtil.spammerMap.containsKey(id)) {
            spammerUtil.addSpammerInMap(spammer, id);
        }
        SpammerThread currentSpammer = spammerUtil.spammerMap.get(id);
        Spammer spammerInsideThread = currentSpammer.getSpammer();
        spammerInsideThread.setTime(spammer.getTime());
    }

    public void editSpamDelay(String json) {
        JsonObject jsonObject = dataExtractor.extractJsonFromString(json);
        String id = dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "id");
        String time = dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "time");

        User user = userUtil.getUser();
        Spammer spammer = spammerUtil.getSpammerByIdAndVerifyByUser(id, user);
        usersRepositoryHandler.free(user);

        int timeLong = Integer.parseInt(htmlTagsClear.removeTags(time));
        if (timeLong < 0) timeLong = 0;
        spammer.setDelay(timeLong);
        spammerRepository.save(spammer);
        editTimeDelaySpammer(spammer);
    }

    private void editTimeDelaySpammer(Spammer spammer) {
        long id = spammer.getId();
        if (!spammerUtil.spammerMap.containsKey(id)) {
            spammerUtil.addSpammerInMap(spammer, id);
        }
        SpammerThread currentSpammer = spammerUtil.spammerMap.get(id);
        Spammer spammerInsideThread = currentSpammer.getSpammer();
        spammerInsideThread.setDelay(spammer.getDelay());
    }
}
