package pl.chatme.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.chatme.dto.ConversationMessageDTO;
import pl.chatme.service.ConversationMessageService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/messages")
public class ConversationMessageController {

    private final ConversationMessageService conversationMessageService;

    public ConversationMessageController(ConversationMessageService conversationMessageService) {
        this.conversationMessageService = conversationMessageService;
    }

    /**
     * {@code GET /messages} : list of last send messages in users conversation.
     *
     * @param conversationId conversation id where sender is authenticated user.
     * @param size           number of returned messages
     * @param principal      authenticated user
     * @return 200 and list of DTOs representation of ConversationMessage entity or 200 and empty list if not exists any message in
     * conversation. 404 if authenticated user not found. 404 if conversation not found.
     */
    @GetMapping(params = {"conversation_id", "size"})
    public ResponseEntity<List<ConversationMessageDTO>> getUserConversationMessages(@RequestParam("conversation_id") long conversationId,
                                                                                    @RequestParam("size") int size,
                                                                                    Principal principal) {
        return ResponseEntity.ok(conversationMessageService.getMessagesWithSize(principal.getName(), conversationId, size));
    }

    /**
     * {@code GET /messages} : list of last send messages in users conversation before time passed in params.
     *
     * @param conversationId conversation id where sender is authenticated user.
     * @param size           number of returned messages
     * @param beforeTime     time before which messages be search
     * @param principal      authenticated user
     * @return 200 and list of DTOs representation of ConversationMessage entity or 200 and empty list if not found any message before
     * time. 400 if beforeTime date format is not supported. 404 if conversation not found.
     * @see pl.chatme.util.DateUtils open to see supported date format.
     */
    @GetMapping(params = {"conversation_id", "size", "before_time"})
    public ResponseEntity<List<ConversationMessageDTO>> getUserConversationMessages(@RequestParam("conversation_id") long conversationId,
                                                                                    @RequestParam("size") int size,
                                                                                    @RequestParam("before_time") String beforeTime,
                                                                                    Principal principal) {

        return ResponseEntity.ok(conversationMessageService.getMessagesWithSizeAndBeforeTime(principal.getName(), conversationId, beforeTime, size));
    }

    /**
     * {@code PATCH /messages} : mark messages as delivered
     *
     * @param conversationId conversation id where recipient is authenticated user.
     * @param principal      authenticated user
     * @return 204 if success, 404 if authenticated user not found, 404 if conversation not found.
     */
    @PatchMapping(params = {"conversation_with_id"})
    public ResponseEntity<Void> markConversationReceivedMessagesAsDelivered(@RequestParam("conversation_with_id") long conversationId,
                                                                            Principal principal) {

        conversationMessageService.setAllRecipientMessagesStatusAsDelivered(conversationId, principal.getName());
        return ResponseEntity.noContent().build();

    }

}
