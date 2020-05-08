package ru.sovaowltv.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserHaveStreamUtil {
    private final StreamRepositoryHandler streamRepositoryHandler;

    public void solveUserHaveStream(Model model, User user) {
        try {
            Optional<Stream> streamOptional = streamRepositoryHandler.getByUserId(user.getId());
            streamOptional.ifPresent(stream -> model.addAttribute("userHaveStream", stream));
        } catch (Exception e) {
            model.addAttribute("userHaveStream", "not null");
        }
    }
}
