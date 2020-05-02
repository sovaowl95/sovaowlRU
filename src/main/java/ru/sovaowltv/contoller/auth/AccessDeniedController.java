package ru.sovaowltv.contoller.auth;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.sovaowltv.model.user.Role;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AccessDeniedController {
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final UserUtil userUtil;

    @GetMapping("/accessDenied")
    public String getErrorPage(Model model) {
        Optional<User> user = Optional.empty();
        try {
            user = userUtil.getUserOptionalFromContext();
            return user.map(this::getRedirectPage).orElseGet(() -> getAccessDenied(model));
        } finally {
            user.ifPresent(usersRepositoryHandler::saveAndFree);
        }
    }

    @NotNull
    private String getAccessDenied(Model model) {
        model.addAttribute("errMessage", "registerFirst");
        return "accessDenied";
    }

    @NotNull
    private String getRedirectPage(User user) {
        if (user.getRoles().contains(Role.REGISTERED)) {
            return "redirect:/profile/email/verification";
        } else {
            SecurityContextHolder.clearContext();
            return "redirect:/login";
        }
    }
}
