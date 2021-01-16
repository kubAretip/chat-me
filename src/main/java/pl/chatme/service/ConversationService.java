package pl.chatme.service;

import pl.chatme.domain.Conversation;

import java.util.Optional;

public interface ConversationService {

    Optional<Conversation> getConversation(long senderUserId, long recipientUserId);

}
