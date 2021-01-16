package pl.chatme.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * A user conversation.
 * Each user pairs have two conversation object because we must identify who send the message and who get it.
 * These two conversation object have reference to each other.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_with_id", nullable = false)
    private Conversation conversationWith;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_recipient_id", nullable = false)
    private User recipient;
}
