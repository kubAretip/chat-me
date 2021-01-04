package pl.chatme.service;

import pl.chatme.domain.User;

public interface SendMailService {
    void sendActivationEmail(User user);
}
