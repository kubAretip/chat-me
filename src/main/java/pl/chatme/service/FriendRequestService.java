package pl.chatme.service;

import pl.chatme.domain.FriendRequest;
import pl.chatme.domain.enumerated.FriendRequestStatus;

import java.util.List;

public interface FriendRequestService {
    FriendRequest createNewFriendsRequest(String senderUsername, String friendRequestCode);

    FriendRequest replyToFriendsRequest(long friendRequestId, String recipientUsername, boolean accept);

    List<FriendRequest> getFriendRequest(String username);

    List<FriendRequest> getSenderFriendsRequestByStatus(String username, FriendRequestStatus status);

    List<FriendRequest> fetchAllFriendsRequestForRecipient(String username);

    void deleteRejectedFriendRequest(FriendRequest friendRequest);
}
