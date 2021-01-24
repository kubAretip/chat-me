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
import pl.chatme.util.Translator;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/friends-request")
public class FriendRequestController {

    private final FriendRequestService friendRequestService;
    private final FriendRequestMapper friendRequestMapper;
    private final ConversationService conversationService;
    private final Translator translator;

    public FriendRequestController(FriendRequestService friendRequestService,
                                   FriendRequestMapper friendRequestMapper,
                                   ConversationService conversationService,
                                   Translator translator) {
        this.friendRequestService = friendRequestService;
        this.friendRequestMapper = friendRequestMapper;
        this.conversationService = conversationService;
        this.translator = translator;
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

        if (status.equalsIgnoreCase(FriendRequestStatus.SENT.name())) {
            return ResponseEntity.ok(
                    friendRequestService.getSenderFriendsRequestByStatus(principal.getName(), FriendRequestStatus.SENT)
                            .stream()
                            .map(friendRequestMapper::mapToFriendRequestDTO)
                            .collect(Collectors.toList()));
        }

        throw Problem.builder()
                .withTitle(translator.translate("exception.invalid.param.title"))
                .withStatus(Status.BAD_REQUEST)
                .withDetail(translator.translate("exception.invalid.searching.status.param.body", new Object[]{status}))
                .build();
    }

    @PostMapping(params = {"invite_code"})
    public ResponseEntity<FriendRequestDTO> createNewFriendsRequest(@RequestParam("invite_code") String inviteCode,
                                                                    Principal principal,
                                                                    UriComponentsBuilder uriComponentsBuilder) {

        var newFriendRequest = friendRequestService.createNewFriendsRequest(principal.getName(), inviteCode);
        var uri = uriComponentsBuilder.path("/friends/{id}").buildAndExpand(newFriendRequest.getId());
        return ResponseEntity.created(uri.toUri())
                .body(friendRequestMapper.mapToFriendRequestDTO(newFriendRequest));

    }

    @PatchMapping(path = "/{id}", params = {"accept"})
    public ResponseEntity<FriendRequestDTO> replyToFriendsRequest(@PathVariable("id") long friendRequestId,
                                                                  @RequestParam("accept") boolean accept,
                                                                  Principal principal) {

        var friendRequest = friendRequestService.replyToFriendsRequest(friendRequestId, principal.getName(), accept);

        if (friendRequest.getStatus().equals(FriendRequestStatus.REJECTED)) {
            // delete friend request
            friendRequestService.deleteRejectedFriendRequest(friendRequest);
            return ResponseEntity.noContent().build();
        }

        // create conversation
        conversationService.createUsersConversation(friendRequest.getSender(), friendRequest.getRecipient());
        return ResponseEntity.ok(friendRequestMapper.mapToFriendRequestDTO(friendRequest));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSentFriendsRequestById(@PathVariable("id") Long id, Principal principal) {
        friendRequestService.deleteFriendRequest(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }


}
