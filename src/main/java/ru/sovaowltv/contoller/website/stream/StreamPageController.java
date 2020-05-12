package ru.sovaowltv.contoller.website.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.multistream.MultiStreamUtil;
import ru.sovaowltv.service.security.SecurityUtil;
import ru.sovaowltv.service.stream.StreamUtil;
import ru.sovaowltv.service.user.UserHaveStreamUtil;
import ru.sovaowltv.service.user.UserUtil;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class StreamPageController {
    private final UserUtil userUtil;
    private final StreamUtil streamUtil;
    private final MultiStreamUtil multiStreamUtil;
    private final SecurityUtil securityUtil;
    private final UserHaveStreamUtil userHaveStreamUtil;

    @GetMapping("/")
    public String getAllStreams(Model model) {
        User user = userUtil.setUserIfExistInModelREADONLY(model);
        userHaveStreamUtil.solveUserHaveStream(model, user);

        List<Stream> streamList = streamUtil.getAllStreams();
        streamList.sort((s1, s2) -> {
            if (s2.isLive() == s1.isLive()) {
                return Integer.compare(s2.getUser().getLevel(), s1.getUser().getLevel());
            } else {
                return Boolean.compare(s2.isLive(), s1.isLive());
            }
        });
        model.addAttribute("streamList", streamList);

        return "streamList";
    }

    @GetMapping("/{streamName}")
    public String getStreamPage(@PathVariable String streamName, Model model) {
        streamUtil.initStreamModelUserData(streamName, model);
        return "streamPage";
    }

    @GetMapping("/{streamName}/chat")
    public String getOnlyChat(@PathVariable String streamName, Model model) {
        streamUtil.initStreamModelUserData(streamName, model);
        return "fragments/chat";
    }

    @GetMapping("/{streamName}/chat/public")
    public String getOnlyChatPublic(@PathVariable String streamName, Model model) {
        Stream stream = streamUtil.getStreamByUserNickname(streamName);
        model.addAttribute("stream", stream);
        return "fragments/chatPublic";
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
}
