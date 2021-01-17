package pl.chatme.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.chatme.service.ConversationMessageService;
import pl.chatme.dto.ConversationMessageDTO;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/messages")
public class ConversationMessageController {

    private final ConversationMessageService conversationMessageService;

    public ConversationMessageController(ConversationMessageService conversationMessageService) {
        this.conversationMessageService = conversationMessageService;
    }

    @GetMapping(params = {"recipient_user_id", "size"})
    public ResponseEntity<List<ConversationMessageDTO>> getMessageWithSize(@RequestParam("recipient_user_id") long recipientId,
                                                                           @RequestParam("size") int size,
                                                                           Principal principal) {
        return ResponseEntity.ok(conversationMessageService.getMessagesWithSize(principal.getName(), recipientId, size));
    }

    @GetMapping(params = {"recipient_user_id", "size", "before_time"})
    public ResponseEntity<List<ConversationMessageDTO>> getOneToOneConversation(@RequestParam("recipient_user_id") long secondUserId,
                                                                                @RequestParam("size") int size,
                                                                                @RequestParam("before_time") String beforeTime,
                                                                                Principal principal) {

        return ResponseEntity.ok(conversationMessageService.getMessagesWithSizeAndBeforeTime(principal.getName(), secondUserId, beforeTime, size));
    }


}
