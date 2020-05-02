package ru.sovaowltv.config.security;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UsersRepositoryHandler usersRepositoryHandler;

    @Override
    public UserDetails loadUserByUsername(String login) {
        User user = null;
        try {
            login = login.toLowerCase();
            user = usersRepositoryHandler.getUserByLoginOrEmail(login, login);
            Set<GrantedAuthority> grantedAuthorities = solveAuthorities(user);
            return new org.springframework.security.core.userdetails.User(
                    user.getLogin(),
                    user.getPassword(),
                    grantedAuthorities);
        } finally {
            usersRepositoryHandler.free(user);
        }
    }


    @NotNull
    private Set<GrantedAuthority> solveAuthorities(User user) {
        return user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }
}
