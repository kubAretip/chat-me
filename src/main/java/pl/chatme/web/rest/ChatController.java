package pl.chatme.web.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;
import pl.chatme.service.ConversationMessageService;
import pl.chatme.service.ConversationService;
import pl.chatme.web.rest.vm.MessageVM;
import pl.chatme.web.rest.vm.SimpleMessageVM;

import java.security.Principal;

@Slf4j
@RestController
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ConversationService conversationService;
    private final ConversationMessageService conversationMessageService;

    public ChatController(SimpMessagingTemplate simpMessagingTemplate,
                          ConversationService conversationService,
                          ConversationMessageService conversationMessageService) {

        this.simpMessagingTemplate = simpMessagingTemplate;
        this.conversationService = conversationService;
        this.conversationMessageService = conversationMessageService;
    }

    /**
     * To send message use url /app/chat and add MessageVM body.
     * In front end part to subscribe message use url /user/{senderId}/queue/messages
     *
     * @param messageVM
     */
    @MessageMapping("/chat")
    public ResponseEntity<String> processMessage(@Payload MessageVM messageVM, Principal principal) {

        conversationService.getConversation(principal.getName(), messageVM.getRecipientId())
                .ifPresent(conversation -> {
                    conversationMessageService.saveConversationMessage(conversation, messageVM.getContent(), messageVM.getTime());
                    simpMessagingTemplate.convertAndSendToUser(conversation.getRecipient().getId().toString(),
                            "/queue/messages", new SimpleMessageVM(conversation.getSender().getLogin(), messageVM.getContent()));
                });
        return ResponseEntity.ok("CONNECTED");
    }
}
