package pl.chatme.service;

import pl.chatme.dto.ConversationDTO;
import pl.chatme.dto.UserDTO;

import java.util.List;

public interface ConversationService {

    ConversationDTO getConversation(String senderUsername, long recipientUserId);

    void createUsersConversation(UserDTO user1, UserDTO user2);

    List<ConversationDTO> getSenderConversation(String username);

    void deleteConversation(String username, long conversationId);
}
