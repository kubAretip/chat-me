package pl.chatme.service;

import pl.chatme.domain.Conversation;
import pl.chatme.domain.User;

import java.util.List;
import java.util.Optional;

public interface ConversationService {

    Optional<Conversation> getConversation(String senderUsername, long recipientUserId);

    void createUsersConversation(User user1, User user2);

    List<Conversation> getSenderConversation(String username);

    void deleteConversation(String username, long conversationId);
}
