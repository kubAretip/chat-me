package pl.chatme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.chatme.domain.Conversation;
import pl.chatme.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link Conversation} entity.
 */
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findBySenderAndRecipient(User sender, User recipient);

    boolean existsBySenderAndRecipient(User sender, User recipient);

    List<Conversation> findBySender(User sender);
}
