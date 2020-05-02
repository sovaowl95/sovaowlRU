package ru.sovaowltv.contoller.website.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.stream.StreamUtil;
import ru.sovaowltv.service.stream.TwitchStreamUtil;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import javax.annotation.security.RolesAllowed;

@Controller
@RequiredArgsConstructor
public class StreamSettingsChatController {
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final UserUtil userUtil;
    private final StreamUtil streamUtil;
    private final TwitchStreamUtil twitchStreamUtil;

    @RolesAllowed("ROLE_ADMIN")
    @PostMapping("/stream/chat/launch/allChats")
    @ResponseStatus(HttpStatus.OK)
    public void launchAllStreamsChats() {
        streamUtil.launchAllStreamsChatsAndSpammers();
    }

    //todo: ANOTHER API SERVICE

    /**
     * TWITCH
     */
    @RolesAllowed("ROLE_ADMIN")
    @PostMapping("/stream/chat/launch/twitchChat/{anotherNick}")
    @ResponseStatus(HttpStatus.OK)
    public void addTwitchChat(@PathVariable String anotherNick) {
        User user = null;
        try {
            user = userUtil.getUser();
            twitchStreamUtil.addAnotherTwitchToChat(user, anotherNick);
        } finally {
            usersRepositoryHandler.free(user);
        }
    }

    @RolesAllowed("ROLE_ADMIN")
    @PostMapping("/stream/chat/launch/twitchChat")
    @ResponseStatus(HttpStatus.OK)
    public void reloadMyTwitchChat() {
        User user = null;
        try {
            user = userUtil.getUser();
            twitchStreamUtil.reloadTwitchStreamChat(user);
        } finally {
            usersRepositoryHandler.free(user);
        }
    }
    /**
     * GG
     */

    /**
     * YOUTUBE
     */

    /**
     * ALL
     */
}