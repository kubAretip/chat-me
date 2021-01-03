package pl.chatme.security;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.chatme.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.stream.Collectors;

/**
 * Authenticate a user from the database.
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) throws UsernameNotFoundException {
        log.debug("Authenticating {}", login);

        var lowerCaseLogin = login.toLowerCase();

        if (new EmailValidator().isValid(login, null)) {
            return userRepository
                    .findOneWithAuthoritiesByEmailIgnoreCase(login)
                    .map(user -> createSpringSecurityUser(lowerCaseLogin, user))
                    .orElseThrow(() -> new UsernameNotFoundException("User with email " + login + " wasn't found in the database."));

        }

        return userRepository.findOneWithAuthoritiesByLoginIgnoreCase(login.toLowerCase())
                .map(user -> createSpringSecurityUser(lowerCaseLogin, user))
                .orElseThrow(() -> new UsernameNotFoundException("User " + login + " wasn't found in the database."));
    }

    private User createSpringSecurityUser(String lowerCaseLogin, pl.chatme.domain.User user) {

        if (!user.getActivated()) {
            throw new UserNotActivatedException("User " + lowerCaseLogin + " wasn't activated.");
        }

        var authorities = user
                .getAuthorities()
                .stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                .collect(Collectors.toList());

        return new User(user.getLogin(), user.getPassword(), authorities);

    }


}
