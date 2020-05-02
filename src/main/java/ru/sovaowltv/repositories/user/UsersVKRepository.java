package ru.sovaowltv.repositories.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.apiauth.UserVK;

import java.util.Optional;

public interface UsersVKRepository extends JpaRepository<UserVK, Long> {
    Optional<UserVK> findBySub(String sub);
}
