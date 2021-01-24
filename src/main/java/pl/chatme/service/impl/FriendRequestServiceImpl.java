package pl.chatme.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.chatme.domain.FriendRequest;
import pl.chatme.domain.enumerated.FriendRequestStatus;
import pl.chatme.exception.AlreadyExistsException;
import pl.chatme.exception.InvalidDataException;
import pl.chatme.exception.NotFoundException;
import pl.chatme.repository.FriendRequestRepository;
import pl.chatme.repository.UserRepository;
import pl.chatme.service.FriendRequestService;
import pl.chatme.util.ExceptionUtils;
import pl.chatme.util.Translator;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
class FriendRequestServiceImpl implements FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final Translator translator;
    private final ExceptionUtils exceptionUtils;

    public FriendRequestServiceImpl(FriendRequestRepository friendRequestRepository,
                                    UserRepository userRepository,
                                    Translator translator,
                                    ExceptionUtils exceptionUtils) {
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
        this.translator = translator;
        this.exceptionUtils = exceptionUtils;
    }

    @Override
    public FriendRequest createNewFriendsRequest(String senderUsername, String friendRequestCode) {

        var senderOptional = userRepository.findOneByLoginIgnoreCase(senderUsername);
        var friendRequestRecipientOptional = userRepository.findByFriendRequestCode(friendRequestCode);

        if (senderOptional.isEmpty())
            throw exceptionUtils.userNotFoundException(senderUsername);

        if (friendRequestRecipientOptional.isEmpty())
            throw new NotFoundException(translator.translate("user.not.found"),
                    translator.translate("exception.user.activation.key.not.found", new Object[]{friendRequestCode}));


        var sender = senderOptional.get();
        var friendRequestRecipient = friendRequestRecipientOptional.get();

        if (sender.getFriendRequestCode().equals(friendRequestCode))
            throw new InvalidDataException(translator.translate("exception.invalid.action"),
                    translator.translate("exception.invalid.sent.yourself"));

        friendRequestRepository.existsFriendsRequestForUsers(sender.getId(), friendRequestRecipient.getId())
                .ifPresent(fr -> {
                    throw new AlreadyExistsException(translator.translate("exception.already.sent.friends.request"),
                            translator.translate("exception.already.sent.friends.request.body"));
                });

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
                        throw new InvalidDataException(translator.translate("exception.invalid.action"),
                                translator.translate("exception.friends.request.already.accepted"));
                    }

                    if (!friendRequest.getRecipient().getLogin().equals(recipientUsername)) {
                        throw new InvalidDataException(translator.translate("exception.invalid.action"),
                                translator.translate("exception.not.recipient"));
                    }

                    if (accept) {
                        friendRequest.setStatus(FriendRequestStatus.ACCEPTED);
                    } else {
                        friendRequest.setStatus(FriendRequestStatus.REJECTED);
                    }
                    return friendRequestRepository.save(friendRequest);
                })
                .orElseThrow((() -> new NotFoundException(translator.translate("exception.friends.request.not.found"),
                        translator.translate("exception.friends.request.not.found.body"))));
    }

    @Override
    public List<FriendRequest> getSenderFriendsRequestByStatus(String username, FriendRequestStatus status) {
        return userRepository.findOneByLoginIgnoreCase(username)
                .map(user -> friendRequestRepository.findBySenderAndStatus(user, status))
                .orElseThrow(() -> exceptionUtils.userNotFoundException(username));
    }

    @Override
    public List<FriendRequest> fetchAllFriendsRequestForRecipient(String username) {
        return userRepository.findOneByLoginIgnoreCase(username)
                .map(user -> friendRequestRepository.findByRecipientAndStatus(user, FriendRequestStatus.SENT))
                .orElseThrow(() -> exceptionUtils.userNotFoundException(username));
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
                            throw new InvalidDataException(translator.translate("exception.invalid.action"),
                                    translator.translate("exception.friends.request.not.owner"));
                        }
                        if (!friendRequest.getStatus().equals(FriendRequestStatus.SENT))
                            throw new InvalidDataException(translator.translate("exception.invalid.action"),
                                    translator.translate("exception.friends.request.cancel.abort",
                                            new Object[]{friendRequest.getStatus().name().toLowerCase()}));

                        friendRequestRepository.delete(friendRequest);
                    });
        } else
            throw exceptionUtils.userNotFoundException(senderUsername);
    }
}
