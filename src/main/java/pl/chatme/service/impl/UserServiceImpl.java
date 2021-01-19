package pl.chatme.service.impl;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.chatme.domain.Authority;
import pl.chatme.domain.User;
import pl.chatme.dto.UserDTO;
import pl.chatme.dto.mapper.UserMapper;
import pl.chatme.repository.AuthorityRepository;
import pl.chatme.repository.UserRepository;
import pl.chatme.security.AuthoritiesConstants;
import pl.chatme.service.UserService;
import pl.chatme.service.exception.AlreadyExistsException;
import pl.chatme.service.exception.InvalidDataException;
import pl.chatme.service.exception.NotFoundException;

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
                        throw new AlreadyExistsException("Incorrect login.", "Login already used.");
                });

        userRepository.findOneByEmailIgnoreCase(userDTO.getEmail())
                .ifPresent(existingUser -> {
                    boolean removed = removeNonActivatedUser(existingUser);
                    if (!removed)
                        throw new AlreadyExistsException("Incorrect email.", "Email is already in use.");
                });

        var newUser = userMapper.mapToUser(userDTO);
        newUser.setPassword(passwordEncoder.encode(password));
        var authorities = new HashSet<Authority>();
        authorityRepository.findById(AuthoritiesConstants.USER.getRole()).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);
        newUser.setActivationKey(RandomStringUtils.randomAlphanumeric(124));
        newUser.setFriendRequestCode(newUser.getLogin().toLowerCase() + RandomStringUtils.randomNumeric(9));
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


    @Override
    public User renewFriendRequestCode(String login) {
        return userRepository.findOneByLoginIgnoreCase(login)
                .map(user -> {
                    user.setFriendRequestCode(generateFriendRequestCode(user.getLogin()));
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new NotFoundException("User not found", "User with login = " + login + " not exists."));
    }

    private boolean removeNonActivatedUser(User existingUser) {
        if (existingUser.getActivated()) {
            return false;
        } else {
            userRepository.delete(existingUser);
            return true;
        }
    }

    private String generateFriendRequestCode(String login) {
        return login.toLowerCase() + "-" + RandomStringUtils.randomNumeric(10);
    }

    @Override
    public User changeUserPassword(String username, String currentPassword, String newPassword) {
        return userRepository.findOneByLoginIgnoreCase(username)
                .map(user -> {
                    if (passwordEncoder.matches(currentPassword, user.getPassword())) {
                        user.setPassword(passwordEncoder.encode(newPassword));
                        return userRepository.save(user);
                    } else {
                        throw new InvalidDataException("Invalid data.", "Incorrect current password");
                    }
                })
                .orElseThrow(() -> new NotFoundException("User not found", "User with login = " + username + " not exists."));
    }

    @Override
    public User getUser(String username) {
        return userRepository.findOneByLoginIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("User not found", "User with login = " + username + " not exists."));
    }
}
