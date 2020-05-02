package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sovaowltv.service.notifications.discord.DiscordUtil;

@Controller
@RequiredArgsConstructor
public class DiscordController {
    private final DiscordUtil discordUtil;

    @GetMapping("/discord/success")
    public String addDiscord(@RequestParam(name = "guild_id") String guildId) {
        return discordUtil.addDiscordNotification(guildId);
    }

    @PostMapping("/discord/remove")
    public String removeDiscord() {
        return discordUtil.removeDiscordNotification();
    }
}
