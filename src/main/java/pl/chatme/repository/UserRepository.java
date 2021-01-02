package pl.chatme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.chatme.domain.User;

import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link User} entity.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findOneWithAuthoritiesByEmailIgnoreCase(String email);

    Optional<User> findOneWithAuthoritiesByLogin(String login);

}
