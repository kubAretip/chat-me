package pl.chatme.service.impl;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import pl.chatme.domain.Authority;
import pl.chatme.domain.User;
import pl.chatme.repository.AuthorityRepository;
import pl.chatme.repository.UserRepository;
import pl.chatme.security.AuthoritiesConstants;
import pl.chatme.service.UserService;
import pl.chatme.service.dto.UserDTO;
import pl.chatme.service.mapper.UserMapper;

import java.util.HashSet;

@Slf4j
@Service
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityRepository authorityRepository;

    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           AuthorityRepository authorityRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
    }

    @Override
    public User createUser(UserDTO userDTO, String password) {

        userRepository.findOneByLoginIgnoreCase(userDTO.getLogin())
                .ifPresent(existingUser -> {
                    boolean removed = removeNonActivatedUser(existingUser);
                    if (!removed)
                        throw Problem.builder()
                                .withStatus(Status.CONFLICT)
                                .withTitle("Invalid username.")
                                .withDetail("Username already used.")
                                .build();
                });

        userRepository.findOneByEmailIgnoreCase(userDTO.getEmail())
                .ifPresent(existingUser -> {
                    boolean removed = removeNonActivatedUser(existingUser);
                    if (!removed)
                        throw Problem.builder()
                                .withStatus(Status.CONFLICT)
                                .withTitle("Invalid email.")
                                .withDetail("Email is already in use.")
                                .build();
                });

        var newUser = userMapper.mapToUser(userDTO);
        newUser.setPassword(passwordEncoder.encode(password));
        var authorities = new HashSet<Authority>();
        authorityRepository.findById(AuthoritiesConstants.USER.getRole()).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);
        newUser.setActivationKey(RandomStringUtils.randomAlphanumeric(124));
        userRepository.save(newUser);

        log.debug("Registered new user {}", newUser);
        return newUser;
    }

    @Override
    public boolean activateUser(String activationKey) {

        if (Strings.isNullOrEmpty(activationKey))
            return false;

        return userRepository.findOneByActivationKey(activationKey)
                .map(user -> {
                    log.debug("Activation user with id {} with key {}", user.getId(), activationKey);
                    user.setActivated(true);
                    user.setActivationKey(null);
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }

    private boolean removeNonActivatedUser(User existingUser) {
        if (existingUser.getActivated()) {
            return false;
        } else {
            userRepository.delete(existingUser);
            return true;
        }
    }

}
