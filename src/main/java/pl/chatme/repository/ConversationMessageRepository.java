package pl.chatme.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.chatme.domain.Conversation;
import pl.chatme.domain.ConversationMessage;
import pl.chatme.domain.User;
import pl.chatme.domain.enumerated.MessageStatus;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for the {@link ConversationMessage} entity.
 */
public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    @Query(
            value = "SELECT * FROM conversation_message " +
                    "WHERE (conversation_id = :c1Id OR conversation_id = :c2Id) AND time < :beforeTime ORDER BY time DESC",
            nativeQuery = true)
    List<ConversationMessage> findUserConversationMessagesBeforeTime(long c1Id, long c2Id, OffsetDateTime beforeTime,
                                                                     Pageable p);

    List<ConversationMessage> findByConversationOrConversation(Conversation c1, Conversation c2, Pageable pageable);

    List<ConversationMessage> findByRecipientAndMessageStatusAndConversation(User recipient, MessageStatus status,
                                                                             Conversation conversation);
}
