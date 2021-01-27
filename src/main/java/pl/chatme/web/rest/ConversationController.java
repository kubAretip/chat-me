package pl.chatme.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.chatme.dto.ConversationDTO;
import pl.chatme.service.ConversationService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * {@code GET /conversations} : get list of conversation where user is a sender.
     *
     * @param principal Authenticated user
     * @return 200 and list of DTOs representation of Conversation entity. 404 if user not found. If user don't have any conversation
     * returns empty list and 200.
     */
    @GetMapping
    public ResponseEntity<List<ConversationDTO>> getUserConversation(Principal principal) {
        return ResponseEntity.ok(conversationService.getSenderConversation(principal.getName()));
    }

    /**
     * {@code DELETE /conversation/{id}} : delete conversation (also delete all messages and accepted friend request)
     * Authenticated user is sender
     *
     * @param conversationId conversation id
     * @param principal      Authenticated user
     * @return 204 if success, 400 if user is not one of the owner of conversation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserConversation(@PathVariable("id") long conversationId, Principal principal) {
        conversationService.deleteConversation(principal.getName(), conversationId);
        return ResponseEntity.noContent().build();
    }

}
