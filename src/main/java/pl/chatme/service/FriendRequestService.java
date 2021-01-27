package pl.chatme.service;

import pl.chatme.domain.enumerated.FriendRequestStatus;
import pl.chatme.dto.FriendRequestDTO;

import java.util.List;

public interface FriendRequestService {
    FriendRequestDTO createNewFriendsRequest(String senderUsername, String friendRequestCode);

    FriendRequestDTO replyToFriendsRequest(long friendRequestId, String recipientUsername, boolean accept);

    List<FriendRequestDTO> getSenderFriendsRequestByStatus(String username, FriendRequestStatus status);

    List<FriendRequestDTO> fetchAllFriendsRequestForRecipient(String username);

    void deleteFriendsRequest(long friendRequestId);

    void deleteFriendRequest(String senderUsername, Long id);
}
