package pl.chatme.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.chatme.domain.enumerated.FriendRequestStatus;

import javax.persistence.*;
import java.time.OffsetDateTime;


@Entity
@Table(name = "friend_request",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_sender_id", "user_recipient_id"}))
@Getter
@Setter
@NoArgsConstructor
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sent_time", nullable = false)
    public OffsetDateTime sentTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_recipient_id", nullable = false)
    private User recipient;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private FriendRequestStatus status;

}
