package ru.sovaowltv.service.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.apiauth.UserTwitch;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.user.UsersTwitchRepository;
import ru.sovaowltv.service.api.token.TwitchTokenHandler;
import ru.sovaowltv.service.api.webhooks.TwitchStreamLiveSub;
import ru.sovaowltv.service.notifications.discord.DiscordNotificationUtil;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;
import ru.sovaowltv.service.stream.StreamUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;
import ru.sovaowltv.service.user.params.UserPremiumUtil;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebsiteHandlerScheduler {
    private final UsersTwitchRepository usersTwitchRepository;
    private final UsersRepositoryHandler usersRepositoryHandler;
    private final StreamRepositoryHandler streamRepositoryHandler;

    private final DiscordNotificationUtil discordNotificationUtil;
    private final UserPremiumUtil userPremiumUtil;
    private final StreamUtil streamUtil;

    private final TwitchTokenHandler twitchTokenHandler;

    private final TwitchStreamLiveSub twitchStreamLiveSub;

    @Scheduled(fixedRate = Long.MAX_VALUE)
    public void tokensTwitchUpdate() {
        List<UserTwitch> users = usersTwitchRepository.findAll();
        users.forEach(twitchTokenHandler::refresh);
    }

    @Scheduled(fixedRate = Long.MAX_VALUE)
    public void tokensGGUpdate() {
        log.error("Update GG tokens");
        //tODO: IMPORTANT!
    }

    @Scheduled(fixedRate = Long.MAX_VALUE)
    public void tokensYTUpdate() {
        log.error("Update youtube tokens");
        //tODO: IMPORTANT!
    }


    @Scheduled(fixedRate = Long.MAX_VALUE)
    public void launchDiscord() {
        log.info("launch discordNotificationService");
        discordNotificationUtil.launch();
    }

    @Scheduled(fixedRate = Long.MAX_VALUE)
    private void launchAllStreamsChats() {
        streamUtil.launchAllStreamsChatsAndSpammers();
    }

    @Scheduled(cron = "0 30 23 * * *")
    private void reSubTwitchWebHook() {
        List<User> allByUserTwitchNotNull = usersRepositoryHandler.findAllByUserTwitchNotNull();
        allByUserTwitchNotNull.forEach(user -> {
            try {
                Optional<Stream> streamOptional = streamRepositoryHandler.getByUserId(user.getId());
                if (streamOptional.isPresent() && streamOptional.get().getUser().getUserTwitch() != null) {
                    if (!twitchStreamLiveSub.subForStream(user, user.getUserTwitch(), streamOptional.get())) {
                        log.warn("resubscribe twitch WebHook {} {}", user.getId(), user.getUserTwitch().getId());
                    }
                }
            } catch (Exception e) {
                log.error("reSubTwitchWebHook problems", e);
            } finally {
                usersRepositoryHandler.free(user);
            }
        });
    }

    @Scheduled(fixedRate = Long.MAX_VALUE)
    @Scheduled(cron = "59 59 4 * * *")
    private void revalidatePremiumsOnLaunchAndTimer() {
        userPremiumUtil.revalidatePremiums();
    }
}