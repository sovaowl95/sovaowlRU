package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.service.commands.CommandsUtil;
import ru.sovaowltv.service.factorys.CommandsFactory;
import ru.sovaowltv.service.stream.StreamUtil;

@Controller
@RequiredArgsConstructor
public class StreamCommandsController {
    private final StreamUtil streamUtil;
    private final CommandsUtil commandsUtil;

    private final CommandsFactory commandsFactory;

    @PostMapping("/stream/commands/create")
    @ResponseStatus(HttpStatus.OK)
    public void createCommand() {
        Stream stream = streamUtil.getStreamByAuthContext();
        commandsFactory.createCommand(stream);
    }

    @PostMapping("/stream/commands/delete")
    @ResponseStatus(HttpStatus.OK)
    public void deleteCommand(@RequestBody String body) {
        Stream stream = streamUtil.getStreamByAuthContext();
        commandsUtil.delete(stream, body);
    }

    @PostMapping("/stream/commands/edit")
    @ResponseStatus(HttpStatus.OK)
    public void saveCommand(@RequestBody String body) {
        Stream stream = streamUtil.getStreamByAuthContext();
        commandsUtil.update(stream, body);
    }
}
