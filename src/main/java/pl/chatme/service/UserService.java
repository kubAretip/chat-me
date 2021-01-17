package pl.chatme.service;

import pl.chatme.domain.User;
import pl.chatme.service.dto.UserDTO;

public interface UserService {
    User createUser(UserDTO userDTO, String password);

    boolean activateUser(String activationKey);

    User renewFriendRequestCode(String username);
}
