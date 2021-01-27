package pl.chatme.service;

import pl.chatme.dto.UserDTO;

public interface UserService {
    UserDTO createUser(UserDTO userDTO, String password);

    void activateUser(String activationKey);

    UserDTO renewFriendRequestCode(String username);

    UserDTO changeUserPassword(String username, String currentPassword, String newPassword);

    UserDTO getUser(String name);

    UserDTO modifyUserInformation(long userId, UserDTO userDTO, String username);
}
