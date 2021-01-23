package pl.chatme.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.chatme.domain.FriendRequest;
import pl.chatme.domain.enumerated.FriendRequestStatus;
import pl.chatme.repository.FriendRequestRepository;
import pl.chatme.repository.UserRepository;
import pl.chatme.service.FriendRequestService;
import pl.chatme.exception.AlreadyExistsException;
import pl.chatme.exception.InvalidDataException;
import pl.chatme.exception.NotFoundException;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
class FriendRequestServiceImpl implements FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    public FriendRequestServiceImpl(FriendRequestRepository friendRequestRepository,
                                    UserRepository userRepository) {
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    public FriendRequest createNewFriendsRequest(String senderUsername, String friendRequestCode) {

        var senderOptional = userRepository.findOneByLoginIgnoreCase(senderUsername);
        var friendRequestRecipientOptional = userRepository.findByFriendRequestCode(friendRequestCode);

        if (senderOptional.isEmpty())
            throw new NotFoundException("User not found.", "User with login " + senderUsername + " not exists.");

        if (friendRequestRecipientOptional.isEmpty())
            throw new NotFoundException("User not found.", "User with friend code " + friendRequestCode + " does not exist.");


        var sender = senderOptional.get();
        var friendRequestRecipient = friendRequestRecipientOptional.get();

        if (sender.getFriendRequestCode().equals(friendRequestCode))
            throw new InvalidDataException("You can't do this!", "You can not send a friend request to yourself.");

        var isAlreadyExistsFriendRequest = friendRequestRepository
                .existsFriendsRequestForUsers(sender.getId(), friendRequestRecipient.getId());

        if (isAlreadyExistsFriendRequest.isPresent()) {
            throw new AlreadyExistsException("Already sent.", "We already register this friends request.");
        }

        var newFriendRequest = new FriendRequest();
        newFriendRequest.setSender(sender);
        newFriendRequest.setRecipient(friendRequestRecipient);
        newFriendRequest.setStatus(FriendRequestStatus.SENT);
        newFriendRequest.setSentTime(OffsetDateTime.now());
        return friendRequestRepository.save(newFriendRequest);

    }

    @Override
    public FriendRequest replyToFriendsRequest(long friendRequestId, String recipientUsername, boolean accept) {
        return friendRequestRepository.findById(friendRequestId)
                .map(friendRequest -> {

                    if (friendRequest.getStatus().equals(FriendRequestStatus.ACCEPTED)) {
                        throw new InvalidDataException("Already accepted.", "You already accepted this friend request.");
                    }

                    if (!friendRequest.getRecipient().getLogin().equals(recipientUsername)) {
                        throw new InvalidDataException("You can't do this.", "You are not the recipient.");
                    }

                    if (accept) {
                        friendRequest.setStatus(FriendRequestStatus.ACCEPTED);
                    } else {
                        friendRequest.setStatus(FriendRequestStatus.REJECTED);
                    }
                    return friendRequestRepository.save(friendRequest);
                })
                .orElseThrow((() -> new NotFoundException("Friend request not found.",
                        "We can not find this friend request.")));
    }

    @Override
    public List<FriendRequest> getSenderFriendsRequestByStatus(String username, FriendRequestStatus status) {
        return userRepository.findOneByLoginIgnoreCase(username)
                .map(user -> friendRequestRepository.findBySenderAndStatus(user, status))
                .orElseThrow(() -> new NotFoundException("User not found.", "User with login " + username + " not exists."));
    }

    @Override
    public List<FriendRequest> fetchAllFriendsRequestForRecipient(String username) {
        return userRepository.findOneByLoginIgnoreCase(username)
                .map(user -> friendRequestRepository.findByRecipientAndStatus(user, FriendRequestStatus.SENT))
                .orElseThrow(() -> new NotFoundException("User not found.", "User with login " + username + " not exists."));
    }


    @Override
    public void deleteRejectedFriendRequest(FriendRequest friendRequest) {
        if (friendRequest.getStatus().equals(FriendRequestStatus.REJECTED)) {
            friendRequestRepository.delete(friendRequest);
        }
    }


    @Override
    public void deleteFriendRequest(String senderUsername, Long id) {
        var senderOptional = userRepository.findOneByLoginIgnoreCase(senderUsername);

        if (senderOptional.isPresent()) {
            var sender = senderOptional.get();

            friendRequestRepository.findById(id)
                    .ifPresent(friendRequest -> {
                        if (!friendRequest.getSender().equals(sender)) {
                            throw new InvalidDataException("Invalid data", "You are not the friend request owner.");
                        }
                        if (!friendRequest.getStatus().equals(FriendRequestStatus.SENT))
                            throw new InvalidDataException("Invalid data",
                                    "You can't cancel friends request with status " + friendRequest.getStatus().name().toLowerCase());

                        friendRequestRepository.delete(friendRequest);
                    });
        } else
            throw new NotFoundException("User not found.", "User with login " + senderUsername + " not exists.");
    }
}
