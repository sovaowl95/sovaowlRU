package ru.sovaowltv.repositories.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.apiauth.UserTwitch;

import java.util.Optional;

public interface UsersTwitchRepository extends JpaRepository<UserTwitch, Long> {
    Optional<UserTwitch> findBySub(String sub);

    Optional<UserTwitch> findByUserTwitchChannelId(String userTwitchChannelId);
}
