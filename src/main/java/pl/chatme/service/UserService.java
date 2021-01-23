package pl.chatme.service;

import pl.chatme.domain.User;
import pl.chatme.dto.UserDTO;

public interface UserService {
    User createUser(UserDTO userDTO, String password);

    void activateUser(String activationKey);

    User renewFriendRequestCode(String username);

    User changeUserPassword(String username, String currentPassword, String newPassword);

    User getUser(String name);
}
