package ru.sovaowltv.service.notifications.discord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.apinotification.DiscordNotification;
import ru.sovaowltv.model.apinotification.NotificationFor;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.service.time.TimeUtil;
import ru.sovaowltv.service.unclassified.KeyWordsReplacerUtil;

import javax.security.auth.login.LoginException;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:api/discord.yml")
public class DiscordNotificationUtil extends ListenerAdapter {
    private JDA jda;
    private boolean connected = false;

    private final KeyWordsReplacerUtil keyWordsReplacerUtil;
    private final TimeUtil timeUtil;

    @Value("${discord_BotToken}")
    private String discordToken;

    public boolean launch() {
        try {
            jda = new JDABuilder(discordToken)
                    .addEventListener(this)
                    .build();
            jda.setAutoReconnect(true);
            connected = true;
            return true;
        } catch (LoginException e) {
            log.error("login in discord failed", e);
            connected = false;
        }
        return false;
    }

    public void sendNotification(Stream stream) {
        waitConnect();
        try {
            DiscordNotification discordNotification = stream.getDiscordNotification();
            if (discordNotification == null) {
                log.debug("Can't find discord for stream " + stream.getUser().getNickname());
                return;
            }
            send(stream, discordNotification);
        } catch (Exception e) {
            log.error("discord send notification error", e);
        }
    }

    private void send(Stream stream, DiscordNotification discordNotification) {
        String server = discordNotification.getServer();
        Guild guildById = jda.getGuildById(server);
        List<TextChannel> textChannelsByName = guildById.getTextChannelsByName(discordNotification.getChannel(), false);
        String message = keyWordsReplacerUtil.replaceAllKeyWords(stream, discordNotification.getText(), NotificationFor.DISCORD);
        textChannelsByName.get(0).sendMessage(message).queue();
    }

    private void waitConnect() {
        while (!connected) {
            while (!launch()) {
                timeUtil.sleepSeconds(5);
            }
        }
    }

    public boolean checkChannelName(String name, Stream stream) {
        try {
            String guildId = stream.getDiscordNotification().getServer();
            Guild guildById = jda.getGuildById(guildId);
            if (guildById == null) return false;
            return !guildById.getTextChannelsByName(name, false).isEmpty();
        } catch (Exception e) {
            log.error("check name error", e);
        }
        return false;
    }

    public List<TextChannel> getAllTextChannels(Stream stream) {
        try {
            String guildId = stream.getDiscordNotification().getServer();
            Guild guildById = jda.getGuildById(guildId);
            return guildById.getTextChannels();
        } catch (Exception e) {
            log.error("get All Text Channels error", e);
        }
        return Collections.emptyList();
    }


    public String getCurrentTextChannel(Stream stream) {
        try {
            return stream.getDiscordNotification().getChannel();
        } catch (Exception e) {
            log.error("get current text channel error", e);
        }
        return "";
    }
}