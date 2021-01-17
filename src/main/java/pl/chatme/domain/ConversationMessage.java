package pl.chatme.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * A message.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "conversation_message")
public class ConversationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_recipient_id", nullable = false)
    private User recipient;

    private String content;

    @Column(nullable = false)
    private OffsetDateTime time;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "message_status")
    private MessageStatus messageStatus;
}
