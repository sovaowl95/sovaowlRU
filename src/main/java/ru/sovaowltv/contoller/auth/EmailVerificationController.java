package ru.sovaowltv.contoller.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.sovaowltv.exceptions.EmailSendingException;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.email.EmailUtil;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationController {
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final UserUtil userUtil;
    private final EmailUtil emailUtil;

    @GetMapping("/profile/email/verification")
    public String getForAuthHands(Model model) {
        Optional<User> userOptional = Optional.empty();
        try {
            userOptional = userUtil.getUserOptionalFromContext();
            if (userOptional.isPresent()) {
                model.addAttribute("user", userOptional.get());
                if (userUtil.isUserActivated(userOptional.get())) {
                    return "redirect:/";
                }
            }
            return "email/emailRegHands";
        } finally {
            userOptional.ifPresent(usersRepositoryHandler::free);
        }
    }


    @PostMapping("/profile/email/verificationByHands/{email}/{code}")
    @ResponseStatus(HttpStatus.OK)
    public void postVerificationByHands(@PathVariable(required = false) String code,
                                        @PathVariable(required = false) String email) {
        User userByEmail = null;
        try {
            userByEmail = usersRepositoryHandler.getUserByEmail(email);
            emailUtil.validateEmailVerification(code, userByEmail);
            userUtil.commitVerification(userByEmail);
            userUtil.setAuthContext(userByEmail);
        } finally {
            usersRepositoryHandler.saveAndFree(userByEmail);
        }
    }

    @PostMapping("/profile/sendEmailAgain/{email}")
    @ResponseStatus(HttpStatus.OK)
    public void sendEmailAgain(@PathVariable String email) {
        User user = null;
        try {
            user = userUtil.getUser();
            if (user.getEmailVerification() == null) return;
            if (emailUtil.sendRegEmail(email, user)) {
                throw new EmailSendingException("Can't send email " + email);
            }
        } finally {
            usersRepositoryHandler.free(user);
        }
    }

    @PostMapping("/profile/changeEmail/{email}")
    @ResponseStatus(HttpStatus.OK)
    public void changeEmail(@PathVariable String email) {
        User user = null;
        try {
            user = userUtil.getUser();
            userUtil.setEmail(user, email);
            if (emailUtil.sendRegEmail(email, user)) {
                throw new EmailSendingException("Can't send email " + email);
            }
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }

    }

    @GetMapping("/profile/email/verification/{userId}/{code}")
    public String getActivateFromEmail(@PathVariable(required = false) String code,
                                       @PathVariable(required = false) String userId,
                                       @RequestParam(required = false) String check,
                                       Model model) {
        User user = null;
        try {
            user = usersRepositoryHandler.getUserById(userId);

            if (userUtil.isUserActivated(user)) {
                return "redirect:/";
            }

            if (check != null) {
                model.addAttribute("user", user);
                return "email/emailRegEmail";
            }

            if (userUtil.isEmailVerificationCodeCorrect(code, user))
                userUtil.commitVerification(user);

            userUtil.setAuthContext(user);
            return "redirect:/profile/email/verification/success";
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }

    @GetMapping("/profile/email/verification/success")
    public String verificationSuccess() {
        User user = null;
        try {
            user = userUtil.getUser();
            userUtil.setAuthContext(user);
            return "email/emailRegSuccess";
        } finally {
            usersRepositoryHandler.free(user);
        }
    }
}
