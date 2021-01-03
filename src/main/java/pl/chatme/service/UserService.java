package pl.chatme.service;

import pl.chatme.domain.User;
import pl.chatme.service.dto.UserDTO;

public interface UserService {
    User registerUser(UserDTO userDTO, String password);
}
