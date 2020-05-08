package ru.sovaowltv.service.commands;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.apinotification.NotificationFor;
import ru.sovaowltv.model.command.Command;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.stream.CommandsRepository;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;
import ru.sovaowltv.service.unclassified.KeyWordsReplacerUtil;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CommandsUtil {
    private final CommandsRepository commandsRepository;
    private final StreamRepositoryHandler streamRepositoryHandler;

    private final KeyWordsReplacerUtil keyWordsReplacerUtil;

    private final DataExtractor dataExtractor;

    public void update(Stream stream, String body) {
        JsonObject jsonObject = dataExtractor.extractJsonFromString(body);
        String id = dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "id");
        boolean enable = dataExtractor.getPrimitiveAsBooleanFromJson(jsonObject, "enabled");
        String keyword = dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "keyWord");
        Set<String> alias = dataExtractor.getSetFromJsonInLoverCase(jsonObject, "alias");
        String action = dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "action");
        boolean forPublicShown = dataExtractor.getPrimitiveAsBooleanFromJson(jsonObject, "forPublicShown");
        boolean needArgs = dataExtractor.getPrimitiveAsBooleanFromJson(jsonObject, "needArgs");
        int argsCount = dataExtractor.getPrimitiveAsIntFromJson(jsonObject, "argsCount");
        int cooldown = dataExtractor.getPrimitiveAsIntFromJson(jsonObject, "cooldown");
        int cost = dataExtractor.getPrimitiveAsIntFromJson(jsonObject, "cost");

        Command command = getCommand(stream, id);

        command.setKeyWord(keyword);
        command.setAlias(alias);
        command.setAction(action);
        command.setForPublicShown(forPublicShown);
        command.setNeedArgs(needArgs);
        command.setArgsCount(argsCount);
        command.setEnabled(enable);
        command.setCooldown(cooldown);
        command.setCost(cost);

        commandsRepository.save(command);
    }


    public void delete(Stream stream, String body) {
        JsonObject jsonObject = dataExtractor.extractJsonFromString(body);
        String id = dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "id");

        Command command = getCommand(stream, id);

        stream.getCommandSet().remove(command);
        command.setStream(null);

        streamRepositoryHandler.save(stream);
        commandsRepository.delete(command);
    }


    private Command getCommand(Stream stream, String id) {
        Command command = commandsRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't find command by id " + id));
        boolean contains = stream.getCommandSet().contains(command);
        if (!contains)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "It's not your command");
        return command;
    }


    public Command getCommand(String login, String text, String channel, User user, Stream stream) {
        String[] split = text.split(" ");
        String keyWord = split[0].toLowerCase();
        Set<Command> commandSet = stream.getCommandSet();
        for (Command command : commandSet) {
            if (command.getKeyWord().equalsIgnoreCase(keyWord)) {
                return command;
            }
            Set<String> alias = command.getAlias();
            if (alias.contains(keyWord)) {
                return command;
            }
        }
        return null;
    }

    public String solveStreamerCommand(String login, String text, String channel, User user, Command command, Stream stream) {
        return keyWordsReplacerUtil.replaceAllKeyWords(stream, command.getAction(), NotificationFor.WEBSITE_COMMAND);
    }
}
