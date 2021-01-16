package pl.chatme.service;

import pl.chatme.domain.ChatMessage;
import pl.chatme.domain.Conversation;
import pl.chatme.service.dto.ChatMessageDTO;

import java.util.List;

public interface ChatMessageService {
    ChatMessage saveChatMessage(Conversation conversation, String content, String time);

    List<ChatMessageDTO> getMessagesWithSizeAndBeforeTime(long firstUserId, long secondUserId, String beforeTime, int to);

    List<ChatMessageDTO> getMessagesWithSize(long firstUserId, long secondUserId, int size);
}
