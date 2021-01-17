package pl.chatme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.chatme.domain.User;

import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link User} entity.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findOneWithAuthoritiesByEmailIgnoreCase(String email);

    Optional<User> findOneWithAuthoritiesByLoginIgnoreCase(String login);

    Optional<User> findOneByLoginIgnoreCase(String login);

    Optional<User> findOneByEmailIgnoreCase(String email);

    Optional<User> findOneByActivationKey(String activationKey);

    Optional<User> findByFriendRequestCode(String friendRequestCode);

}
