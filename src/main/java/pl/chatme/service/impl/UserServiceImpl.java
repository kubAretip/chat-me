package pl.chatme.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.chatme.domain.Authority;
import pl.chatme.domain.User;
import pl.chatme.repository.AuthorityRepository;
import pl.chatme.repository.UserRepository;
import pl.chatme.security.AuthoritiesConstants;
import pl.chatme.service.UserService;
import pl.chatme.service.error.EmailAlreadyTakenException;
import pl.chatme.service.error.UsernameAlreadyTakenException;
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
    public User registerUser(UserDTO userDTO, String password) {

        userRepository.findOneByLoginIgnoreCase(userDTO.getLogin())
                .ifPresent(existingUser -> {
                    boolean removed = removeNonActivatedUser(existingUser);
                    if (!removed)
                        throw new UsernameAlreadyTakenException();
                });

        userRepository.findOneByEmailIgnoreCase(userDTO.getEmail())
                .ifPresent(existingUser -> {
                    boolean removed = removeNonActivatedUser(existingUser);
                    if (!removed)
                        throw new EmailAlreadyTakenException();
                });

        var newUser = userMapper.mapToUser(userDTO);
        newUser.setPassword(passwordEncoder.encode(password));
        var authorities = new HashSet<Authority>();
        authorityRepository.findById(AuthoritiesConstants.USER.getRole()).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);

        log.debug("Registered new user {}", newUser);
        return newUser;
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
