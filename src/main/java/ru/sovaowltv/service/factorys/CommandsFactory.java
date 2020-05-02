package ru.sovaowltv.service.factorys;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.command.Command;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.repositories.stream.CommandsRepository;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class CommandsFactory {
    private final CommandsRepository commandsRepository;
    private final StreamRepositoryHandler streamRepositoryHandler;

    public Command createCommand(Stream stream) {
        Command command = new Command();
        command.setEnabled(false);
        command.setKeyWord("name");
        command.setAlias(new HashSet<>());
        command.getAlias().add("!def1 !def2");
        command.setAction("default action");
        command.setForPublicShown(false);
        command.setNeedArgs(false);
        command.setArgsCount(0);
        command.setCooldown(0);
        command.setCost(0);

        command.setStream(stream);
        stream.getCommandSet().add(command);

        commandsRepository.save(command);
        streamRepositoryHandler.save(stream);

        return command;
    }
}
