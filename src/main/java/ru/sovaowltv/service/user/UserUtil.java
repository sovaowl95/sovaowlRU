package ru.sovaowltv.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.sovaowltv.exceptions.user.UserNotFoundException;
import ru.sovaowltv.model.user.Role;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.unclassified.UserDataValidator;

import java.security.Principal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUtil {
    private final UserHaveStreamUtil userHaveStreamUtil;
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final UserDataValidator userDataValidator;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public User setUserInModelREADONLY(Model model) {
        User user = null;
        try {
            user = usersRepositoryHandler.getUserByLogin(getLoginFromSecurityContext());
            model.addAttribute("user", user);
            userHaveStreamUtil.solveUserHaveStream(model, user);
            return user;
        } catch (Exception e) {
            return null;
        } finally {
            usersRepositoryHandler.free(user);
        }
    }

    public User getUserREADONLY() {
        User user = null;
        try {
            user = usersRepositoryHandler.getUserByLogin(getLoginFromSecurityContext());
            return user;
        } catch (Exception e) {
            throw new UserNotFoundException("User not found - getUserREADONLY null");
        } finally {
            usersRepositoryHandler.free(user);
        }
    }

    public User getUser() {
        return usersRepositoryHandler.getUserByLogin(getLoginFromSecurityContext());
    }

    public void setNewPassword(String password, String id) {
        User user = null;
        try {
            user = usersRepositoryHandler.getUserById(id);
            userDataValidator.checkPassword(password);
            user.setPassword(bCryptPasswordEncoder.encode(password));
            user.setRecoveryToken(UUID.randomUUID().toString());
        } finally {
            usersRepositoryHandler.saveAndFree(user);
        }
    }

    private String getLoginFromSecurityContext() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private boolean isAdmin(User user) {
        return user.getRoles().contains(Role.ADMIN);
    }

    private boolean isModerator(User user) {
        return user.getRoles().contains(Role.MODERATOR);
    }

    public boolean isAdminOrModerator(User user) {
        return isAdmin(user) || isModerator(user);
    }

    public void commitVerification(User user) {
        Set<Role> roles = user.getRoles();
        roles.remove(Role.REGISTERED);
        roles.add(Role.USER);
        user.setEmailVerification(null);
    }

    public void banUser(User user) {
        user.getRoles().clear();
        user.getRoles().add(Role.BANNED);
        SecurityContextHolder.clearContext();
    }

    public void setAuthContext(User user) {
        List<GrantedAuthority> list = new ArrayList<>();
        user.getRoles().forEach(role -> list.add(new SimpleGrantedAuthority("ROLE_" + role.name())));
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getLogin(), null, list);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void setAuthContext(String login) {
        User user = null;
        try {
            user = usersRepositoryHandler.getUserByLogin(login);
            setAuthContext(user);
        } finally {
            usersRepositoryHandler.free(user);
        }
    }

    public boolean isUserActivated(User user) {
        return !user.getRoles().contains(Role.REGISTERED) && !user.getRoles().isEmpty();
    }

    public boolean isEmailVerificationCodeCorrect(String code, User user) {
        return user != null && user.getEmailVerification() != null && user.getEmailVerification().equals(code) && user.getRoles().contains(Role.REGISTERED);
    }

    public void setEmail(User user, String email) {
        user.setEmail(email);
    }

    public void setAuthContextIfItsEmpty(Principal principal) {
        try {
            Optional<User> userOptional = getUserOptionalFromContext();
            usersRepositoryHandler.free(userOptional.get());
        } catch (Exception e) {
            setAuthContext(principal.getName());
        }
    }

    public Optional<User> getUserOptionalFromContext() {
        return usersRepositoryHandler.getUserOptionalByLogin(getLoginFromSecurityContext());
    }
}
