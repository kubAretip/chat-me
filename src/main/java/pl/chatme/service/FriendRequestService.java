package pl.chatme.service;

import pl.chatme.domain.FriendRequest;
import pl.chatme.domain.enumerated.FriendRequestStatus;

import java.util.List;

public interface FriendRequestService {
    FriendRequest sendFriendRequest(String senderUsername, String friendRequestCode);

    FriendRequest replyToFriendRequest(long friendRequestId, String recipientUsername, boolean accept);

    List<FriendRequest> getFriendRequest(String username);

    List<FriendRequest> getSenderFriendRequestByStatus(String username, FriendRequestStatus status);

    void deleteRejectedFriendRequest(FriendRequest friendRequest);
}
