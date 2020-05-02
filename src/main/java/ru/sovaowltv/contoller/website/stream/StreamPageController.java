package ru.sovaowltv.contoller.website.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.security.SecurityUtil;
import ru.sovaowltv.service.stream.StreamUtil;
import ru.sovaowltv.service.user.UserHaveStreamUtil;
import ru.sovaowltv.service.user.UserUtil;

import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class StreamPageController {
    private final SecurityUtil securityUtil;
    private final StreamUtil streamUtil;
    private final UserUtil userUtil;
    private final UserHaveStreamUtil userHaveStreamUtil;

    @GetMapping("/")
    public String getAllStreams(Model model) {
        User user = userUtil.setUserInModelREADONLY(model);
        userHaveStreamUtil.solveUserHaveStream(model, user);
        model.addAttribute("streamList", streamUtil.getAllStreams());
        return "streamList";
    }

    @GetMapping("/{streamName}")
    public String getStreamPage(@PathVariable String streamName, Model model) {
        streamUtil.initStreamModelWithUserData(streamName, model);
        return "streamPage";
    }

    @GetMapping("/{streamName}/chat")
    public String getOnlyChat(@PathVariable String streamName, Model model) {
        streamUtil.initStreamModelWithUserData(streamName, model);
        return "fragments/chat";
    }

    @GetMapping("/{streamName}/publicchat")
    public String getOnlyChatPublic(@PathVariable String streamName, Model model) {
        Stream stream = streamUtil.getStreamByUserNickname(streamName);
        model.addAttribute("stream", stream);
        return "fragments/chatPublic";
    }

    @GetMapping("/stream/settings")
    public String getStreamSettingsPage(Model model, HttpSession session) {
        securityUtil.generateSecTokenStateForSession(session, model);
        User user = userUtil.setUserInModelREADONLY(model);
        model.addAttribute("stream", streamUtil.getStreamByUserNickname(user.getNickname()));
        return "stream/settings/streamSettings";
    }
}
