package pl.chatme.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.chatme.domain.Conversation;
import pl.chatme.domain.ConversationMessage;

import java.time.LocalDateTime;
import java.util.List;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    // TODO : simplify
    @Query(
            value = "SELECT * FROM conversation_message " +
                    "WHERE ((user_sender_id = :sender AND user_recipient_id = :recipient) " +
                    "OR (user_sender_id = :recipient AND user_recipient_id = :sender)) AND time < :beforeTime " +
                    "ORDER BY time DESC",
            nativeQuery = true)
    List<ConversationMessage> findUserConversationMessagesBeforeTime(long sender, long recipient,
                                                                     LocalDateTime beforeTime,
                                                                     Pageable pageable);

    List<ConversationMessage> findByConversationOrConversation(Conversation c1, Conversation c2, Pageable pageable);

}
