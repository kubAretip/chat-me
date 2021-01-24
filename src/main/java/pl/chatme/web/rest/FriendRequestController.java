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

    /**
     * {@code GET /friends-request} : get friends request with SENT status that have been sent to the user.
     * Authenticated user is recipient.
     *
     * @param principal authenticated user
     * @return if success 200 and list of DTOs representation of FriendRequest entity or 200 and empty list if not exists ant friends
     * request, 404 if authenticated user not found
     */
    @GetMapping
    public ResponseEntity<List<FriendRequestDTO>> getReceivedFriendRequests(Principal principal) {
        return ResponseEntity.ok(friendRequestService.fetchAllFriendsRequestForRecipient(principal.getName())
                .stream()
                .map(friendRequestMapper::mapToFriendRequestDTO)
                .collect(Collectors.toList()));
    }

    /**
     * {@code GET /friends-request} : get friends request with SENT status that have been sent by user. Authenticated user is sender.
     *
     * @param status    status of friends request
     * @param principal authenticated user
     * @return if success 200 and list of DTOs representation of FriendRequest entity or 200 and empty list if not exists ant friends
     * request, 400 if status is not supported, 404 if authenticated user not found
     */
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

    /**
     * {@code POST /friends-request} : create new friends request
     *
     * @param inviteCode           friends request code generated during registration
     * @param principal            authenticated user
     * @param uriComponentsBuilder uri builder needed to build location header
     * @return 204 if success and DTO representation of FriendRequest entity, 404 if not found user with invite code, 400 when the
     * owner of friends request code is authenticated user, 409 if already sent or recipient already sent to authenticated user
     */
    @PostMapping(params = {"invite_code"})
    public ResponseEntity<FriendRequestDTO> createNewFriendsRequest(@RequestParam("invite_code") String inviteCode,
                                                                    Principal principal,
                                                                    UriComponentsBuilder uriComponentsBuilder) {

        var newFriendRequest = friendRequestService.createNewFriendsRequest(principal.getName(), inviteCode);
        var uri = uriComponentsBuilder.path("/friends/{id}").buildAndExpand(newFriendRequest.getId());
        return ResponseEntity.created(uri.toUri())
                .body(friendRequestMapper.mapToFriendRequestDTO(newFriendRequest));

    }

    /**
     * {@code PATCH /friends-request/{id}} : reply to friends request. Only recipient of friend request can provide the answer.
     * Authenticated user is recipient
     *
     * @param friendRequestId friends request id
     * @param accept          true = accept friends request, false = reject friends request
     * @param principal       authenticated user
     * @return 200 if accepted and DTO representation of FriendRequest entity, 204 if rejected, 400 if already accepted, 400 if
     * authenticated user is not recipient, 404 if friends request not found.
     */
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

    /**
     * {@code DELETE /friends-request/{id}} : cancel friends request with status SENT. Only for sender.
     * Authenticated user is sender.
     *
     * @param id        friends request id
     * @param principal authenticated user
     * @return 204 if success, 400 if authenticated user is not sender, 400 if friends request status != SENT
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSentFriendsRequestById(@PathVariable("id") Long id, Principal principal) {
        friendRequestService.deleteFriendRequest(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }


}
