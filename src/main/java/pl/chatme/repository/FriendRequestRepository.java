package pl.chatme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.chatme.domain.FriendRequest;
import pl.chatme.domain.User;
import pl.chatme.domain.enumerated.FriendRequestStatus;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    @Query(
            value = "SELECT * FROM friend_request " +
                    "WHERE (user_sender_id=:senderId AND user_recipient_id=:recipientId) OR " +
                    "(user_sender_id=:recipientId AND user_recipient_id=:senderId)",
            nativeQuery = true
    )
    Optional<FriendRequest> existsFriendsRequestForUsers(Long senderId, Long recipientId);

    @Query(value =
            "SELECT * FROM friend_request " +
                    "WHERE (user_sender_id=:userId OR user_recipient_id=:userId) " +
                    "AND status = 'SENT'",
            nativeQuery = true)
    List<FriendRequest> findSentFriendRequestByRecipientIdOrSenderId(long userId);

    List<FriendRequest> findBySenderAndStatus(User sender, FriendRequestStatus status);

    List<FriendRequest> findByRecipientAndStatus(User recipient, FriendRequestStatus status);

}
