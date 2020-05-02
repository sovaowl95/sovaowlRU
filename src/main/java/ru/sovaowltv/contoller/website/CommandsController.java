package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.sovaowltv.service.user.UserUtil;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CommandsController {
    private final UserUtil userUtil;

    private static final List<String> userCommands = List.of(
            "user.rob",
            "user.img",
            "user.yt",
            "user.webm",
            "user.video",
            "user.coub",
            "user.slot",
            "user.help");

    private static final List<String> moderatorCommands = List.of(
            "moderator.ban",
            "moderator.unban",
            "moderator.timeout",
            "moderator.untimeout",
            "moderator.purge",
            "moderator.clear",
            "moderator.clearAll"
    );

    private static final List<String> streamerCommands = List.of(
            "streamer.mod",
            "streamer.unmod"
    );

    private static final List<List<String>> commandsList = List.of(
            userCommands,
            moderatorCommands,
            streamerCommands
    );

    @GetMapping("/commands")
    public String getCommands(Model model) {
        userUtil.setUserInModelREADONLY(model);
        model.addAttribute("commandsList", commandsList);
        return "commands";
    }
}
