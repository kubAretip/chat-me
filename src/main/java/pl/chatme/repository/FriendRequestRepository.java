package pl.chatme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.chatme.domain.FriendRequest;
import pl.chatme.domain.User;
import pl.chatme.domain.enumerated.FriendRequestStatus;

import java.util.List;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    boolean existsBySenderAndRecipientOrRecipientAndSender(User s1, User r1, User s2, User r2);

    @Query(value =
            "SELECT * FROM friend_request " +
                    "WHERE (user_sender_id=:userId OR user_recipient_id=:userId) " +
                    "AND status = 'SENT'",
            nativeQuery = true)
    List<FriendRequest> findSentFriendRequestByRecipientIdOrSenderId(long userId);

    List<FriendRequest> findBySenderAndStatus(User sender, FriendRequestStatus status);

}
