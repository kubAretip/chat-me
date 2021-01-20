package pl.chatme.service.impl;

import org.springframework.stereotype.Service;
import pl.chatme.domain.FriendRequest;
import pl.chatme.domain.enumerated.FriendRequestStatus;
import pl.chatme.repository.FriendRequestRepository;
import pl.chatme.repository.UserRepository;
import pl.chatme.service.FriendRequestService;
import pl.chatme.service.exception.AlreadyExistsException;
import pl.chatme.service.exception.InvalidDataException;
import pl.chatme.service.exception.NotFoundException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    public FriendRequest sendFriendRequest(String senderUsername, String friendRequestCode) {

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
                .existsBySenderAndRecipientOrRecipientAndSender(sender, friendRequestRecipient, friendRequestRecipient, sender);

        if (isAlreadyExistsFriendRequest) {
            throw new AlreadyExistsException("Already sent.", "Friend request was already sent.");
        }

        var newFriendRequest = new FriendRequest();
        newFriendRequest.setSender(sender);
        newFriendRequest.setRecipient(friendRequestRecipient);
        newFriendRequest.setStatus(FriendRequestStatus.SENT);
        newFriendRequest.setSentTime(OffsetDateTime.now());
        return friendRequestRepository.save(newFriendRequest);

    }

    @Override
    public FriendRequest replyToFriendRequest(long friendRequestId, String recipientUsername, boolean accept) {
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
    public List<FriendRequest> getFriendRequest(String username) {
        return userRepository.findOneByLoginIgnoreCase(username)
                .map(user -> friendRequestRepository.findSentFriendRequestByRecipientIdOrSenderId(user.getId()))
                .orElseThrow(() -> new NotFoundException("User not found.", "User with login " + username + " not exists."));
    }
}
