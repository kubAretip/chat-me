package pl.chatme.service;

import pl.chatme.dto.UserDTO;

public interface SendMailService {
    void sendActivationEmail(UserDTO user);
}
