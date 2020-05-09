package ru.sovaowltv.service.stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.sovaowltv.exceptions.stream.NotYourStreamException;
import ru.sovaowltv.exceptions.stream.StreamNotFoundException;
import ru.sovaowltv.exceptions.user.UserNotFoundException;
import ru.sovaowltv.model.multistream.MultiStream;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.multistream.MultiStreamRepository;
import ru.sovaowltv.service.shop.ShopUtil;
import ru.sovaowltv.service.spammer.SpammerUtil;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:api/twitch.yml")
public class StreamUtil {
    private final UsersRepositoryHandler usersRepositoryHandler;
    private final StreamRepositoryHandler streamRepositoryHandler;
    private final MultiStreamRepository multiStreamRepository;

    private final UserUtil userUtil;
    private final SpammerUtil spammerUtil;
    private final ShopUtil shopUtil;

    private final TwitchStreamUtil twitchStreamUtil;
    private final GGStreamUtil ggStreamUtil;
    private final YTStreamUtil ytStreamUtil;

    private static final String STREAM_NOT_FOUND = "Can't find stream";

    public List<Stream> getAllStreams() {
        return streamRepositoryHandler.getAll();
    }

    public Stream getStreamByUserNickname(String streamName) {
        User userByNickname = null;
        try {
            userByNickname = usersRepositoryHandler.getUserByNickname(streamName);

            Optional<Stream> streamByUserId = streamRepositoryHandler.getByUserId(userByNickname.getId());
            if (streamByUserId.isEmpty()) {
                log.error("{} streamByUserId is empty", streamName);
                throw new StreamNotFoundException(STREAM_NOT_FOUND + " " + streamName);
            }

            return streamByUserId.get();
        } catch (UserNotFoundException e) {
            throw new StreamNotFoundException(STREAM_NOT_FOUND + " " + streamName);
        } finally {
            usersRepositoryHandler.free(userByNickname);
        }
    }

    public Stream getStreamByAuthContext() {
        Optional<Stream> streamOptional = streamRepositoryHandler.getByUser(userUtil.getUserREADONLY());

        if (streamOptional.isEmpty()) {
            throw new StreamNotFoundException(STREAM_NOT_FOUND + " " + null);
        }
        return streamOptional.get();
    }

    public void initStreamModelUserData(String streamName, Model model) {
        Stream stream = getStreamByUserNickname(streamName);
        model.addAttribute("stream", stream);
        model.addAttribute("donateFor", stream.getUser());
        isInMultiStream(stream, model);
        initStreamModelUserData(model);
    }

    public void initStreamModelUserData(Model model) {
        User user = userUtil.setUserInModelREADONLY(model);
        model.addAttribute("userStyles", user != null ? user.getStyles() : Collections.emptySet());
        model.addAttribute("userSmiles", user != null ? user.getSmiles() : Collections.emptySet());

        model.addAttribute("rarityOrder", Rarity.values());
        model.addAttribute("smiles", shopUtil.getSmilesList());
        model.addAttribute("styles", shopUtil.getStylesList());
    }

    //todo: ANOTHER API SERVICE
    public void launchAllStreamsChatsAndSpammers() {
        log.info("launch all streams chats");
        streamRepositoryHandler.getAll().forEach(stream -> {
            User user = stream.getUser();
            twitchStreamUtil.launchTwitchChat(user, stream);
            ggStreamUtil.launchGGChat(user);
            ytStreamUtil.launchYTChat(user);
            spammerUtil.launchSpammers(stream);
        });
    }

    public void save(Stream stream) {
        streamRepositoryHandler.save(stream);
    }

    public void isYourStream(Stream stream) {
        User userREADONLY = userUtil.getUserREADONLY();
        if (!stream.getUser().equals(userREADONLY)) {
            throw new NotYourStreamException(
                    "not your stream " + stream.getUser().getId() + ":" + userREADONLY.getId());
        }
    }

    public void isInMultiStream(Stream stream, Model model) {
        List<MultiStream> multiStreamList = multiStreamRepository.findAll();
        for (MultiStream ms : multiStreamList) {
            if (ms.getStreamSet().contains(stream)) {
                model.addAttribute("multiStream", ms);
                break;
            }
        }
    }
}
