package pl.chatme.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.chatme.domain.FriendRequest;
import pl.chatme.domain.enumerated.FriendRequestStatus;
import pl.chatme.dto.FriendRequestDTO;
import pl.chatme.dto.mapper.FriendRequestMapper;
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
import java.util.stream.Collectors;

@Slf4j
@Service
class FriendRequestServiceImpl implements FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final Translator translator;
    private final ExceptionUtils exceptionUtils;
    private final FriendRequestMapper friendRequestMapper;

    public FriendRequestServiceImpl(FriendRequestRepository friendRequestRepository,
                                    UserRepository userRepository,
                                    Translator translator,
                                    ExceptionUtils exceptionUtils,
                                    FriendRequestMapper friendRequestMapper) {
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
        this.translator = translator;
        this.exceptionUtils = exceptionUtils;
        this.friendRequestMapper = friendRequestMapper;
    }

    @Override
    public FriendRequestDTO createNewFriendsRequest(String senderUsername, String friendRequestCode) {

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
        friendRequestRepository.save(newFriendRequest);
        return friendRequestMapper.mapToFriendRequestDTO(newFriendRequest);

    }

    @Override
    public FriendRequestDTO replyToFriendsRequest(long friendRequestId, String recipientUsername, boolean accept) {
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
                    friendRequestRepository.save(friendRequest);
                    return friendRequestMapper.mapToFriendRequestDTO(friendRequest);
                })
                .orElseThrow((() -> new NotFoundException(translator.translate("exception.friends.request.not.found"),
                        translator.translate("exception.friends.request.not.found.body"))));
    }

    @Override
    public List<FriendRequestDTO> getSenderFriendsRequestByStatus(String username, FriendRequestStatus status) {
        var user = userRepository.findOneByLoginIgnoreCase(username)
                .orElseThrow(() -> exceptionUtils.userNotFoundException(username));
        return friendRequestRepository.findBySenderAndStatus(user, status)
                .stream()
                .map(friendRequestMapper::mapToFriendRequestDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendRequestDTO> fetchAllFriendsRequestForRecipient(String username) {
        var user = userRepository.findOneByLoginIgnoreCase(username)
                .orElseThrow(() -> exceptionUtils.userNotFoundException(username));

        return friendRequestRepository.findByRecipientAndStatus(user, FriendRequestStatus.SENT)
                .stream()
                .map(friendRequestMapper::mapToFriendRequestDTO)
                .collect(Collectors.toList());
    }


    @Override
    public void deleteFriendsRequest(long friendRequestId) {
        friendRequestRepository.deleteById(friendRequestId);
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
