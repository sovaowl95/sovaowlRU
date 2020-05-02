package ru.sovaowltv.service.unclassified;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sovaowltv.exceptions.user.*;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserDataValidator {
    private final UsersRepositoryHandler usersRepositoryHandler;

    public void validateAllData(String login, String password, String email, String gender, String rules, boolean needVerification) {
        checkLogin(login);
        checkPassword(password);
        if (needVerification) checkEmail(email);
        checkGender(gender);
        checkRules(rules);
    }

    private void checkLogin(String login) {
        if (login == null || login.isEmpty()) {
            throw new BadLoginException("Bad Login " + login);
        }
        if (login.equalsIgnoreCase("anonymoususer")) {
            throw new BadLoginException("Name reserved " + login);
        }
        Optional<User> userByLogin = usersRepositoryHandler.getUserOptionalByLogin(login);
        if (userByLogin.isPresent()) {
            usersRepositoryHandler.free(userByLogin.get());
            throw new LoginAlreadyInUseException("Login already in use" + login);
        }
    }

    public void checkPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new BadPasswordException("Bad Password");
        }
    }

    private void checkEmail(String email) {
        if (email == null || email.isEmpty() || !email.contains("@")) {
            throw new BadLoginException("Bad Email " + email);
        }
        Optional<User> userByEmail = usersRepositoryHandler.getUserOptionalByEmail(email);
        if (userByEmail.isPresent()) {
            usersRepositoryHandler.free(userByEmail.get());
            throw new EmailAlreadyInUseException("Email already in use " + email);
        }
    }

    private void checkGender(String gender) {
        if (gender == null || gender.isEmpty() || (!gender.equals("true") && !gender.equals("false"))) {
            throw new BadGenderException("Bad Gender " + gender);
        }
    }

    private void checkRules(String rules) {
        if (rules == null || rules.isEmpty() || !rules.equals("true")) {
            throw new BadRulesException("You must accept rules " + rules);
        }
    }
}
