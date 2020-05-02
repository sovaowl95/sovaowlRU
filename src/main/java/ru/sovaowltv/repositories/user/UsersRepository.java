package ru.sovaowltv.repositories.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.user.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByLogin(String login);

    Optional<User> findUserByEmail(String email);

    Optional<User> findUserByNickname(String nickName);

    Optional<User> findUserByLoginOrEmail(String login, String email);

    List<User> findAllByUserTwitchNotNull();

    List<User> findAllByPremiumExpiredBeforeAndPremiumUserTrue(LocalDate time);
}
