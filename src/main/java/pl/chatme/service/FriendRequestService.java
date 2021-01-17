package pl.chatme.service;

import pl.chatme.domain.FriendRequest;

import java.util.List;

public interface FriendRequestService {
    FriendRequest sendFriendRequest(String senderUsername, String friendRequestCode);

    FriendRequest replyToFriendRequest(long friendRequestId, String recipientUsername, boolean accept);

    List<FriendRequest> getFriendRequest(String username);
}
