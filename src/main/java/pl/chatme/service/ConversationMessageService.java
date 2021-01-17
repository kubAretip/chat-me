package pl.chatme.service;

import pl.chatme.domain.ConversationMessage;
import pl.chatme.domain.Conversation;
import pl.chatme.service.dto.ConversationMessageDTO;

import java.util.List;

public interface ConversationMessageService {
    ConversationMessage saveConversationMessage(Conversation conversation, String content, String time);

    List<ConversationMessageDTO> getMessagesWithSizeAndBeforeTime(String senderUsername, long recipientId, String beforeTime, int size);

    List<ConversationMessageDTO> getMessagesWithSize(String senderUsername, long recipientId, int size);
}
