package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.unclassified.Constants;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ProfileController {
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final UserUtil userUtil;
    private final Constants constants;

    @GetMapping(value = {"/profile/{userNickname}", "/profile"})
    public String getProfile(Model model, @PathVariable(required = false) String userNickname) {
        Optional<User> targetUserOptional = Optional.empty();
        try {
            User user = userUtil.setUserInModelREADONLY(model);
            String issuerNickName = user.getNickname();

            if (userNickname == null || userNickname.isEmpty()) {
                return "redirect:/profile/" + issuerNickName;
            } else {
                targetUserOptional = usersRepositoryHandler.getUserByNicknameOptional(userNickname);
                if (targetUserOptional.isEmpty()) {
                    return "redirect:/profile/" + issuerNickName;
                } else {
                    model.addAttribute("userP", targetUserOptional.get());
                    model.addAttribute("levelExpMultiplier", constants.getLevelExpMultiplier());
                    return "profilePage";
                }
            }
        } finally {
            targetUserOptional.ifPresent(usersRepositoryHandler::free);
        }
    }
}
