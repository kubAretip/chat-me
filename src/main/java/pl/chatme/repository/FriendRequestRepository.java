package pl.chatme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pl.chatme.domain.FriendRequest;
import pl.chatme.domain.User;
import pl.chatme.domain.enumerated.FriendRequestStatus;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link FriendRequest} entity.
 */
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    @Query(
            value = "SELECT * FROM friend_request " +
                    "WHERE (user_sender_id=:senderId AND user_recipient_id=:recipientId) OR " +
                    "(user_sender_id=:recipientId AND user_recipient_id=:senderId)",
            nativeQuery = true
    )
    Optional<FriendRequest> existsFriendsRequestForUsers(Long senderId, Long recipientId);

    List<FriendRequest> findBySenderAndStatus(User sender, FriendRequestStatus status);

    List<FriendRequest> findByRecipientAndStatus(User recipient, FriendRequestStatus status);

    @Modifying
    @Query(value = "DELETE FROM friend_request WHERE (user_sender_id=:user1 AND user_recipient_id=:user2)" +
            "OR (user_sender_id=:user2 AND user_recipient_id=:user1)", nativeQuery = true)
    void deleteFriendRequestByUsers(Long user1, Long user2);

}
