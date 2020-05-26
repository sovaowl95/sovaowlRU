package ru.sovaowltv.contoller.website.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.factorys.StreamFactory;
import ru.sovaowltv.service.multistream.MultiStreamUtil;
import ru.sovaowltv.service.notifications.NotificationUtil;
import ru.sovaowltv.service.security.SecurityUtil;
import ru.sovaowltv.service.stream.StreamSettingsUtil;
import ru.sovaowltv.service.stream.StreamUtil;
import ru.sovaowltv.service.user.UserUtil;

import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class StreamSettingsController {
    private final UserUtil userUtil;
    private final StreamUtil streamUtil;
    private final NotificationUtil notificationUtil;
    private final StreamSettingsUtil streamSettingsUtil;
    private final MultiStreamUtil multiStreamUtil;
    private final SecurityUtil securityUtil;

    private final StreamFactory streamFactory;

    @PostMapping("/{streamName}/delete")
    @ResponseStatus(HttpStatus.OK)
    public void deleteStream(@PathVariable String streamName) {
        streamSettingsUtil.deleteStream(streamName);
    }

    @PostMapping("/createStream")
    @ResponseStatus(HttpStatus.OK)
    public void createStream() {
        streamFactory.createStream();
    }

    @PostMapping("/notifyThemAll")
    @ResponseStatus(HttpStatus.OK)
    public void notifyThemAll() {
        Stream stream = streamUtil.getStreamByAuthContext();
        notificationUtil.notifyAll(stream);
    }

    @GetMapping("/stream/settings")
    public String getStreamSettingsPage(Model model, HttpSession session) {
        securityUtil.generateSecTokenStateForSession(session, model);
        User user = userUtil.setUserIfExistInModelREADONLY(model);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("stream", streamUtil.getStreamByUserNickname(user.getNickname()));
        multiStreamUtil.setMSIfExist(model, user);
        return "stream/settings/streamSettings";
    }

    @PostMapping("/stream/settings/changeStatus")
    @ResponseStatus(HttpStatus.OK)
    public void changeStatus(@RequestBody String body) {
        Stream stream = streamUtil.getStreamByAuthContext();
        streamSettingsUtil.changeStreamStatus(stream, body);
    }

    @PostMapping("/stream/settings/changeStreamName")
    @ResponseStatus(HttpStatus.OK)
    public void changeStreamName(@RequestBody String json) {
        streamSettingsUtil.changeStreamName(json);
    }

    @PostMapping("/stream/settings/changeStreamGame")
    @ResponseStatus(HttpStatus.OK)
    public void changeGame(@RequestBody String json) {
        streamSettingsUtil.changeGame(json);
    }

    @PostMapping("/stream/settings/changeChatDailyInfo")
    @ResponseStatus(HttpStatus.OK)
    public void changeChatDailyInfo(@RequestBody String json) {
        streamSettingsUtil.changeChatDailyInfo(json);
    }
}