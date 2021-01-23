package pl.chatme.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import pl.chatme.domain.enumerated.FriendRequestStatus;
import pl.chatme.dto.FriendRequestDTO;
import pl.chatme.dto.mapper.FriendRequestMapper;
import pl.chatme.service.ConversationService;
import pl.chatme.service.FriendRequestService;
import pl.chatme.service.exception.AlreadyExistsException;
import pl.chatme.service.exception.InvalidDataException;
import pl.chatme.service.exception.NotFoundException;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/friends-request")
public class FriendRequestController {

    private final FriendRequestService friendRequestService;
    private final FriendRequestMapper friendRequestMapper;
    private final ConversationService conversationService;

    public FriendRequestController(FriendRequestService friendRequestService,
                                   FriendRequestMapper friendRequestMapper,
                                   ConversationService conversationService) {
        this.friendRequestService = friendRequestService;
        this.friendRequestMapper = friendRequestMapper;
        this.conversationService = conversationService;
    }

    @GetMapping
    public ResponseEntity<List<FriendRequestDTO>> getReceivedFriendRequests(Principal principal) {
        return ResponseEntity.ok(friendRequestService.fetchAllFriendsRequestForRecipient(principal.getName())
                .stream()
                .map(friendRequestMapper::mapToFriendRequestDTO)
                .collect(Collectors.toList()));
    }


    @GetMapping(params = {"status"})
    public ResponseEntity<List<FriendRequestDTO>> getSentFriendsRequestByStatus(@RequestParam("status") String status,
                                                                                Principal principal) {
        try {
            if (status.equalsIgnoreCase(FriendRequestStatus.SENT.name())) {
                return ResponseEntity.ok(
                        friendRequestService.getSenderFriendsRequestByStatus(principal.getName(), FriendRequestStatus.SENT)
                                .stream()
                                .map(friendRequestMapper::mapToFriendRequestDTO)
                                .collect(Collectors.toList()));
            }
        } catch (NotFoundException ex) {
            throw Problem.builder()
                    .withTitle(ex.getTitle())
                    .withStatus(Status.NOT_FOUND)
                    .withDetail(ex.getLocalizedMessage())
                    .build();
        }
        throw Problem.builder()
                .withTitle("Invalid param status value")
                .withStatus(Status.BAD_REQUEST)
                .withDetail("We not support searching by status = " + status)
                .build();
    }

    @PostMapping(params = {"invite_code"})
    public ResponseEntity<FriendRequestDTO> createNewFriendsRequest(@RequestParam("invite_code") String inviteCode,
                                                                    Principal principal,
                                                                    UriComponentsBuilder uriComponentsBuilder) {

        try {
            var newFriendRequest = friendRequestService.createNewFriendsRequest(principal.getName(), inviteCode);
            var uri = uriComponentsBuilder.path("/friends/{id}").buildAndExpand(newFriendRequest.getId());
            return ResponseEntity.created(uri.toUri())
                    .body(friendRequestMapper.mapToFriendRequestDTO(newFriendRequest));
        } catch (NotFoundException ex) {
            throw Problem.builder()
                    .withTitle(ex.getTitle())
                    .withStatus(Status.NOT_FOUND)
                    .withDetail(ex.getLocalizedMessage())
                    .build();
        } catch (AlreadyExistsException ex) {
            throw Problem.builder()
                    .withStatus(Status.CONFLICT)
                    .withTitle(ex.getTitle())
                    .withDetail(ex.getLocalizedMessage())
                    .build();
        } catch (InvalidDataException ex) {
            throw Problem.builder()
                    .withStatus(Status.BAD_REQUEST)
                    .withTitle(ex.getTitle())
                    .withDetail(ex.getLocalizedMessage())
                    .build();
        }
    }

    @PatchMapping(path = "/{id}", params = {"accept"})
    public ResponseEntity<FriendRequestDTO> replyToFriendsRequest(@PathVariable("id") long friendRequestId,
                                                                  @RequestParam("accept") boolean accept,
                                                                  Principal principal) {
        try {
            var friendRequest = friendRequestService.replyToFriendsRequest(friendRequestId, principal.getName(), accept);

            if (friendRequest.getStatus().equals(FriendRequestStatus.REJECTED)) {
                // delete friend request
                friendRequestService.deleteRejectedFriendRequest(friendRequest);
                return ResponseEntity.noContent().build();
            }

            // create conversation
            conversationService.createUsersConversation(friendRequest.getSender(), friendRequest.getRecipient());
            return ResponseEntity.ok(friendRequestMapper.mapToFriendRequestDTO(friendRequest));

        } catch (NotFoundException ex) {
            throw Problem.builder()
                    .withTitle(ex.getTitle())
                    .withStatus(Status.NOT_FOUND)
                    .withDetail(ex.getLocalizedMessage())
                    .build();
        } catch (InvalidDataException ex) {
            throw Problem.builder()
                    .withStatus(Status.BAD_REQUEST)
                    .withTitle(ex.getTitle())
                    .withDetail(ex.getLocalizedMessage())
                    .build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSentFriendsRequestById(@PathVariable("id") Long id, Principal principal) {
        try {
            friendRequestService.deleteFriendRequest(principal.getName(), id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException ex) {
            throw Problem.builder()
                    .withTitle(ex.getTitle())
                    .withStatus(Status.NOT_FOUND)
                    .withDetail(ex.getLocalizedMessage())
                    .build();
        } catch (InvalidDataException ex) {
            throw Problem.builder()
                    .withStatus(Status.BAD_REQUEST)
                    .withTitle(ex.getTitle())
                    .withDetail(ex.getLocalizedMessage())
                    .build();
        }

    }


}
