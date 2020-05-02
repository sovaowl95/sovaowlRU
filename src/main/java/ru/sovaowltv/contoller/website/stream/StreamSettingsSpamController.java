package ru.sovaowltv.contoller.website.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sovaowltv.model.spammer.Spammer;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.spammer.SpammerEditingUtil;
import ru.sovaowltv.service.spammer.SpammerUtil;
import ru.sovaowltv.service.stream.StreamUtil;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

@Controller
@RequiredArgsConstructor
public class StreamSettingsSpamController {
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final UserUtil userUtil;
    private final SpammerUtil spammerUtil;
    private final SpammerEditingUtil spammerEditingUtil;
    private final StreamUtil streamUtil;

    @PostMapping("/stream/settings/spam/create")
    @ResponseStatus(HttpStatus.OK)
    public void createSpam() {
        Stream stream = streamUtil.getStreamByAuthContext();
        spammerUtil.createSpam(stream);
    }

    @PostMapping("/stream/settings/spam/delete")
    @ResponseStatus(HttpStatus.OK)
    public void deleteSpam(@RequestBody String json) {
        spammerUtil.deleteSpam(json);
    }

    @PostMapping("/stream/settings/spam/start/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void startSpam(@PathVariable String id) {
        User user = null;
        try {
            user = userUtil.getUser();
            Spammer spammer = spammerUtil.getSpammerByIdAndVerifyByUser(id, user);
            spammerUtil.startSpammer(spammer);
        } finally {
            usersRepositoryHandler.free(user);
        }
    }

    @PostMapping("/stream/settings/spam/stop/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void stopSpam(@PathVariable String id) {
        User user = null;
        try {
            user = userUtil.getUser();
            Spammer spammer = spammerUtil.getSpammerByIdAndVerifyByUser(id, user);
            spammerUtil.stopSpammer(spammer);
        } finally {
            usersRepositoryHandler.free(user);
        }
    }


    @PostMapping("/stream/settings/spam/edit/text")
    @ResponseStatus(HttpStatus.OK)
    public void editSpamText(@RequestBody String json) {
        spammerEditingUtil.editSpamText(json);
    }


    @PostMapping("/stream/settings/spam/edit/time")
    @ResponseStatus(HttpStatus.OK)
    public void editSpamTime(@RequestBody String json) {
        spammerEditingUtil.editSpamTime(json);
    }

    @PostMapping("/stream/settings/spam/edit/delay")
    @ResponseStatus(HttpStatus.OK)
    public void editSpamDelay(@RequestBody String json) {
        spammerEditingUtil.editSpamDelay(json);
    }
}
