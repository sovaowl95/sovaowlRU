package ru.sovaowltv.service.chat;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.chat.ChatMessage;
import ru.sovaowltv.model.chat.Message;
import ru.sovaowltv.model.chat.MessageStatus;
import ru.sovaowltv.model.command.Command;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.messages.MessageRepository;
import ru.sovaowltv.repositories.website.StylesRepository;
import ru.sovaowltv.service.caravan.CaravanUtil;
import ru.sovaowltv.service.chat.realization.ApiTimeouts;
import ru.sovaowltv.service.commands.CommandsUtil;
import ru.sovaowltv.service.messages.MessageApiDeliver;
import ru.sovaowltv.service.messages.MessageValidationStatus;
import ru.sovaowltv.service.messages.MessageValidator;
import ru.sovaowltv.service.messages.MessagesUtil;
import ru.sovaowltv.service.smiles.GGSmiles;
import ru.sovaowltv.service.smiles.TwitchSmiles;
import ru.sovaowltv.service.smiles.WebSiteSmileAbstract;
import ru.sovaowltv.service.smiles.YTSmiles;
import ru.sovaowltv.service.stream.StreamModerationUtil;
import ru.sovaowltv.service.stream.moderation.*;
import ru.sovaowltv.service.unclassified.HtmlTagsClear;
import ru.sovaowltv.service.unclassified.LanguageUtil;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.params.UserCoinsUtil;
import ru.sovaowltv.service.user.params.UserExpUtil;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static ru.sovaowltv.service.unclassified.Constants.*;

@Service
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:constants.yml")
public class ChatUtil {
    private final MessageRepository messageRepository;
    private final StylesRepository stylesRepository;

    private final StreamModerationUtil streamModerationUtil;
    private final UserUtil userUtil;
    private final UserExpUtil userExpUtil;
    private final UserCoinsUtil userCoinsUtil;
    private final CaravanUtil caravanUtil;
    private final MessagesUtil messagesUtil;
    private final AntiSpamUtil antiSpamUtil;
    private final BanUtil banUtil;
    private final UnBanUtil unBanUtil;
    private final TimeoutUtil timeoutUtil;
    private final UnTimeoutUtil unTimeoutUtil;
    private final ModUtil modUtil;
    private final UnModUtil unModUtil;
    private final PurgeUtil purgeUtil;
    private final ClearUtil clearUtil;
    private final LanguageUtil languageUtil;

    private final DefaultCommands defaultCommands;
    private final CommandsUtil commandsUtil;

    private final TwitchSmiles twitchSmiles;
    private final GGSmiles ggSmiles;
    private final YTSmiles ytSmiles;

    private final MessageValidator messageValidator;
    private final MessageApiDeliver messageApiDeliver;
    private final WebSiteSmileAbstract webSiteSmilesUtil;

    private final HtmlTagsClear htmlTagsClear;

    private final ApiTimeouts apiTimeouts;

    private final Set<String> usersForCoins = Collections.synchronizedSet(new HashSet<>());

    @Value("${website}")
    private String website;

    @Value("${err}")
    private String error;

    public boolean userAllowedSendMessages(String channel, String messageType, User user, Stream stream) {
        if (!messageType.equalsIgnoreCase("history") && !messageType.equalsIgnoreCase(MOD_ACTION)) {
            if (!streamModerationUtil.canChatInChannelBan(user, stream)) {
                messagesUtil.createAndSendMessageStatus(INFO_BAN, "pages.chat.message.youBanned", user.getLogin(), channel);
                return false;
            }

            if (!streamModerationUtil.canChatInChannelTimeout(user, stream)) {
                messagesUtil.createAndSendSimpleMessageStatus(INFO_TIMEOUT, getInfo(user, stream), user.getLogin(), channel);
                return false;
            }

            if (!antiSpamUtil.isAntiSpamOk(user)) {
                messagesUtil.createAndSendSimpleMessageStatus(
                        INFO_SPAM,
                        languageUtil.getStringFor("pages.chat.message.spam")
                                .concat(" <span class='spanTime'>")
                                .concat(antiSpamUtil.getTimeUntilUnblock(user))
                                .concat("</span> ")
                                .concat(languageUtil.getStringFor("pages.chat.message.youTimeoutTimeUnit")),
                        user.getLogin(),
                        channel
                );
                return false;
            }
        }
        return true;
    }

    @NotNull
    private String getInfo(User user, Stream stream) {
        String firstPart = languageUtil.getStringFor("pages.chat.message.youTimeout");
        LocalDateTime localDateTime = apiTimeouts.getTimeoutsByStreamId(stream.getId()).get(user);
        long between = ChronoUnit.SECONDS.between(LocalDateTime.now(), localDateTime);
        String time = String.valueOf(between);
        String lastPart = languageUtil.getStringFor("pages.chat.message.youTimeoutTimeUnit");

        return firstPart + " " + time + " " + lastPart;
    }

    public ChatMessage solveMessageMessage(String channel, Map<String, Object> map, User user, Stream stream) {
        ChatMessage chatMessage = solveMessage(String.valueOf(map.get("text")).trim(), channel, user, stream);
        if (chatMessage instanceof Message) {
            messageRepository.save(((Message) chatMessage));
            userExpUtil.addExpAndPrint(String.valueOf(user.getId()), 1, channel);
            usersForCoins.add(String.valueOf(user.getId()));
        }
        return chatMessage;
    }

    private ChatMessage solveMessage(String text, String channel, User user, Stream stream) {
        ChatMessage message = prepareMessage(text, channel, user, stream);
        if (message instanceof Message)
            messageApiDeliver.sendMessageToAllApiChats(((Message) message), channel, this, user, stream);
        return message;

    }

    public ChatMessage solveCommandMessage(String channel, Map<String, Object> map, User user, Stream stream) {
        ChatMessage message = solveCommand(user.getLogin(), String.valueOf(map.get("text")).trim(), channel, user, stream);
        if (message instanceof Message)
            messageRepository.save(((Message) message));
        return message;
    }

    private ChatMessage solveCommand(String login, String text, String channel, User user, Stream stream) {
        ChatMessage chatMessage = prepareMessage(text, channel, user, stream);
        if (chatMessage instanceof Message) {
            Message message = ((Message) chatMessage);
            if (message.getText().startsWith("!")) {
                message.setOriginalMessage(message.getText());
                if (message.getText().equalsIgnoreCase("!грабить") || message.getText().equalsIgnoreCase("!rob")) {
                    message.setText(defaultCommands.robCommand(user));
                    return caravanCommandAnswer(login, channel, message);
                } else if (message.getText().toLowerCase().startsWith("!img ")) {
                    message.setText(defaultCommands.imgCommand(message.getText()));
                } else if (message.getText().toLowerCase().startsWith("!yt ") || message.getText().toLowerCase().startsWith("!youtube ")) {
                    message.setText(defaultCommands.youtubeCommand(message.getText()));
                } else if (message.getText().toLowerCase().startsWith("!webm ")) {
                    message.setText(defaultCommands.videoCommand(message.getText()));
                } else if (message.getText().toLowerCase().startsWith("!video ")) {
                    message.setText(defaultCommands.videoCommand(message.getText()));
                } else if (message.getText().toLowerCase().startsWith("!coub ")) {
                    message.setText(defaultCommands.coubCommand(message.getText()));
                } else if (message.getText().toLowerCase().startsWith("!slot")) {
                    messageRepository.delete(message);
                    String slotAnswer = defaultCommands.slotCommand(user, message.getText());
                    if (slotAnswer.startsWith("SLOT -> NOT ENOUGH MONEY")) {
                        messagesUtil.sendErrorMessageToLogin(channel, SLOT_NOT_ENOUGH_MONEY, "", login);
                        return null;
                    } else { // ok
                        MessageStatus slotStartMS = messagesUtil.getOkMessageStatus(SLOT_START, "");
                        messagesUtil.convertAndSendToUser(login, channel, slotStartMS);

                        slotAnswer = slotAnswer.replace("SLOT -> ", "");
                        MessageStatus slotRewardMessage = messagesUtil.getOkMessageStatus(SLOT_RES, slotAnswer);
                        messagesUtil.convertAndSend(channel, slotRewardMessage);
                        return null;
                    }
                } else {
                    Command command = commandsUtil.getCommand(login, text, channel, user, stream);
                    if (command != null) {
                        if (command.isForPublicShown()) {
                            String messageText = commandsUtil.solveStreamerCommand(login, text, channel, user, command, stream);
                            MessageStatus messageStatus = messagesUtil.getOkMessageStatus(COMMAND_ANSWER_OK, messageText);
                            messageStatus.setStreamId(message.getId());
                            messagesUtil.convertAndSend(channel, messageStatus);
                            return message;
                        } else {
                            messageRepository.delete(message);
                            String messageText = commandsUtil.solveStreamerCommand(login, text, channel, user, command, stream);
                            MessageStatus messageStatus = messagesUtil.getOkMessageStatus(COMMAND_ANSWER_OK, messageText);
                            messagesUtil.convertAndSendToUser(login, channel, messageStatus);
                            return null;
                        }
                    } else {
                        message.setText("ERR -> Command not found.");
                    }
                }
            }

            if (message.getText().startsWith("ERR ->")) {
                MessageStatus messageStatus = messagesUtil.getErrorMessageStatus(COMMAND_ANSWER_ERROR, message.getText());
                messageRepository.delete(message);
                messagesUtil.convertAndSendToUser(login, channel, messageStatus);
                return null;
            } else {
                messageApiDeliver.sendMessageToAllApiChats(message, channel, this, user, stream);
                return message;
            }
        } else {
            return chatMessage;
        }
    }


    private ChatMessage caravanCommandAnswer(String login, String channel, Message message) {
        messageRepository.delete(message);
        switch (message.getText()) {
            case "CARAVAN -> " + CARAVAN_JOIN:
                MessageStatus messageStatus = new MessageStatus();
                messageStatus.setType(CARAVAN_JOIN);
                Map<String, Object> map = new HashMap<>();
                map.put("username", login);
                map.put("rarity", caravanUtil.getCaravanRarityName());
                messageStatus.setInfo(new Gson().toJson(map));
                messagesUtil.convertAndSendToUser(login, channel, messageStatus);
                return null;
            case "CARAVAN -> " + CARAVAN_JOIN_ERR_ALREADY_IN_JOIN:
                messagesUtil.sendErrorMessageToLogin(channel, CARAVAN_JOIN_ERR_ALREADY_IN_JOIN, message.getText(), login);
                return null;
            case "CARAVAN -> " + CARAVAN_JOIN_ERR_STATUS_JOIN:
                messagesUtil.sendErrorMessageToLogin(channel, CARAVAN_JOIN_ERR_STATUS_JOIN, message.getText(), login);
                return null;
            case "CARAVAN -> " + CARAVAN_JOIN_NOT_ENOUGH_MONEY:
                messagesUtil.sendErrorMessageToLogin(channel, CARAVAN_JOIN_NOT_ENOUGH_MONEY, message.getText(), login);
                return null;
            default:
                messagesUtil.sendErrorMessageToLogin(channel, "err", message.getText(), login);
                return null;
        }
    }


    public ChatMessage solveModActionMessage(String channel, Map<String, Object> map, User user, Stream stream) {
        String secondType = String.valueOf(map.get("secondType"));
        MessageStatus chatMessage = solveModAction(String.valueOf(map.get("text")).trim(), channel, secondType, user, stream);
        if (chatMessage == null) {
            return null;
        } else if (chatMessage.getInfo().startsWith(error)) {
            messagesUtil.convertAndSendToUser(user.getLogin(), channel, chatMessage);
            return null;
        } else {
            return chatMessage;
        }
    }

    private MessageStatus solveModAction(String text, String channel, String secondType, User user, Stream stream) {
        if (text.startsWith("/")) {
            if (text.startsWith("/help")) {
                sendHelpAnswer(user.getLogin(), channel);
                return null;
            } else if (streamModerationUtil.canModerateStream(user, stream)) {
                if (text.startsWith("/ban ") || text.startsWith("/b ")) {
                    if (secondType.equalsIgnoreCase("null")) {
                        return banUtil.banUserByNickName(user, text, channel);
                    } else if (secondType.equalsIgnoreCase("id")) {
                        return banUtil.banUserByMessageId(user, text, channel);
                    } else
                        return unknownTypeMethod();
                } else if (text.startsWith("/unban ") || text.startsWith("/ub ")) {
                    if (secondType.equalsIgnoreCase("null")) {
                        return unBanUtil.unBanUserByNickName(user, text, channel);
                    } else if (secondType.equalsIgnoreCase("id"))
                        return unBanUtil.unBanUserByMessageId(user, text, channel);
                    else
                        return unknownTypeMethod();
                } else if (text.startsWith("/timeout ") || text.startsWith("/t ")) {
                    if (secondType.equalsIgnoreCase("null"))
                        return timeoutUtil.timeoutUserByNickName(user, text, channel);
                    else if (secondType.equalsIgnoreCase("id"))
                        return timeoutUtil.timeoutUserByMessageId(user, text, channel);
                    else
                        return unknownTypeMethod();
                } else if (text.startsWith("/untimeout ") || text.startsWith("/ut ")) {
                    if (secondType.equalsIgnoreCase("null"))
                        return unTimeoutUtil.unTimeoutUserByNickName(user, text, channel);
                    else if (secondType.equalsIgnoreCase("id"))
                        return unTimeoutUtil.unTimeoutUserByMessageId(user, text, channel);
                    else
                        return unknownTypeMethod();
                } else if (text.startsWith("/clear ") || text.startsWith("/c ")) {
                    if (secondType.equalsIgnoreCase("null"))
                        return clearUtil.clearUserByNickName(user, text, channel);
                    else if (secondType.equalsIgnoreCase("id"))
                        return clearUtil.clearUserByMessageId(user, text, channel);
                    else
                        return unknownTypeMethod();
                } else if (text.startsWith("/mod ") || text.startsWith("/m ")) {
                    if (secondType.equalsIgnoreCase("null"))
                        return modUtil.modUserByNickName(user, text, channel);
                    else if (secondType.equalsIgnoreCase("id"))
                        return modUtil.modUserByMessageId(user, text, channel);
                    else
                        return unknownTypeMethod();
                } else if (text.startsWith("/unmod ") || text.startsWith("/um ")) {
                    if (secondType.equalsIgnoreCase("null"))
                        return unModUtil.unmodUserByNickName(user, text, channel);
                    else if (secondType.equalsIgnoreCase("id"))
                        return unModUtil.unmodUserByMessageId(user, text, channel);
                    else
                        return unknownTypeMethod();
                } else if (text.startsWith("/purge ") || text.startsWith("/p ")) {
                    if (secondType.equalsIgnoreCase("null"))
                        return purgeUtil.purgeUserByNickName(user, text, channel);
                    else if (secondType.equalsIgnoreCase("id"))
                        return purgeUtil.purgeUserByMessageId(user, text, channel);
                    else
                        return unknownTypeMethod();
                } else if (text.startsWith("/clearAll") || text.startsWith("/ca")) {
                    return clearUtil.clearAll(user, text, stream, channel);
                } else {
                    return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                            languageUtil.getStringFor("pages.chat.message.moderator.unknownCommand"));
                }
            } else {
                return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                        languageUtil.getStringFor("pages.chat.message.moderator.insufficientPermission"));
            }
        } else {
            return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                    languageUtil.getStringFor("pages.chat.message.moderator.commandMustStartWithSlash"));
        }
    }

    private void sendHelpAnswer(String login, String channel) {
        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setType(INFO_HELP);
        String faq = languageUtil.getStringFor("pages.commands.title");
        messageStatus.setInfo("<a href='/commands'>" + faq + "</a>");
        messagesUtil.convertAndSendToUser(login, channel, messageStatus);
    }

    private MessageStatus unknownTypeMethod() {
        return messagesUtil.getErrorMessageStatus(MOD_ACTION,
                languageUtil.getStringFor("pages.chat.message.moderator.unknownType"));
    }

    //todo: ANOTHER API SERVICE
    private ChatMessage prepareMessage(String text, String channel, User user, Stream stream) {
        if (text.trim().isEmpty() || text.trim().isBlank()) return null;
        Message message = new Message();
        message.setSource(website);
        message.setType("message");
        message.setText(htmlTagsClear.removeTags(text));
        message.setTime(LocalDateTime.now());

        prepareSmiles(message);

        message.setStyle(stylesRepository.findById(user.getUserSettings().getStyleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Style not found"))
                .getName());
        message.setIssuerId(String.valueOf(user.getId()));
        message.setNick(user.getNickname());
        message.setIcons(user.getUserSettings().getActiveIcons()
                .stream()
                .map(icons -> "achievements/" + icons.getName() + ".png")
                .collect(Collectors.joining(" "))
        );
        message.setPremText(user.getUserSettings().isPremiumChat());
        message.setPremiumUser(user.isPremiumUser());

        message.setStreamId(stream.getId());

        message.setModerator(stream.getModeratorsList().contains(user));
        message.setCanControlMod(streamModerationUtil.canModerateStreamAsOwner(user, stream));
        message.setGlobalAdmin(userUtil.isAdminOrModerator(user));

        message.setLevel(user.getLevel());


        MessageValidationStatus messageValidationStatus = messageValidator.validateMessage(message);
        if (messageValidationStatus == MessageValidationStatus.SPAM) {
            return banUtil.banUserByMessageId(stream.getUser(), text, channel);
        }
        if (messageValidationStatus != MessageValidationStatus.OK) {
            MessageStatus messageStatus = new MessageStatus();
            messageStatus.setType("ValidationError");
            messageStatus.setInfo(String.valueOf(messageValidationStatus));
            return messageStatus;
        }
        return message;
    }

    private void prepareSmiles(Message message) {
        if (message.getTwitchSmilesInfo() == null || message.getTwitchSmilesInfo().isEmpty()) {
            message.setTwitchSmilesInfo(streamModerationUtil.generateSmilesInfo(message.getText(), twitchSmiles));
        }

        if (message.getGgSmilesInfo() == null || message.getGgSmilesInfo().isEmpty()) {
            message.setGgSmilesInfo(streamModerationUtil.generateSmilesInfo(message.getText(), ggSmiles));
        }

        if (message.getYtSmilesInfo() == null || message.getYtSmilesInfo().isEmpty()) {
            message.setYtSmilesInfo(streamModerationUtil.generateSmilesInfo(message.getText(), ytSmiles));
        }

        message.setWebSiteSmilesInfo(streamModerationUtil.generateSmilesInfo(message.getText(), webSiteSmilesUtil));
    }

    public void solveIsUserModeratorMessage(String channel, User user, Stream stream) {
        messagesUtil.convertAndSendToUser(user.getLogin(), channel, streamModerationUtil.canModerateStream(user, stream));
    }

    @Scheduled(fixedRate = 1000 * 60)
    public void addExpAndCoins() {
        usersForCoins.forEach(userId -> userCoinsUtil.addCoins(userId, 1));
        usersForCoins.clear();
    }
}
