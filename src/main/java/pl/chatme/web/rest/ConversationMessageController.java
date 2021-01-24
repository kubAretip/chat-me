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

    @GetMapping(params = {"conversation_id", "size"})
    public ResponseEntity<List<ConversationMessageDTO>> getUserConversationMessages(@RequestParam("conversation_id") long conversationId,
                                                                                    @RequestParam("size") int size,
                                                                                    Principal principal) {
        return ResponseEntity.ok(conversationMessageService.getMessagesWithSize(principal.getName(), conversationId, size));
    }

    @GetMapping(params = {"conversation_id", "size", "before_time"})
    public ResponseEntity<List<ConversationMessageDTO>> getUserConversationMessages(@RequestParam("conversation_id") long conversationId,
                                                                                    @RequestParam("size") int size,
                                                                                    @RequestParam("before_time") String beforeTime,
                                                                                    Principal principal) {

        return ResponseEntity.ok(conversationMessageService.getMessagesWithSizeAndBeforeTime(principal.getName(), conversationId, beforeTime, size));
    }

    @PatchMapping(params = {"conversation_with_id"})
    public ResponseEntity<Void> markConversationReceivedMessagesAsDelivered(@RequestParam("conversation_with_id") long conversationId,
                                                                            Principal principal) {

        conversationMessageService.setAllRecipientMessagesStatusAsDelivered(conversationId, principal.getName());
        return ResponseEntity.noContent().build();

    }

}
