package ru.sovaowltv.repositories.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.apiauth.UserGoogle;

import java.util.Optional;


public interface UsersGoogleRepository extends JpaRepository<UserGoogle, Long> {
    Optional<UserGoogle> findBySub(String sub);
}
