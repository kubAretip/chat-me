package pl.chatme.web.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;
import pl.chatme.dto.ConversationMessageDTO;
import pl.chatme.service.ConversationMessageService;
import pl.chatme.service.ConversationService;

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
     * {@code /app/chat} : exposed web socket endpoint for sending messages. In this endpoint client send message.
     * {@code /user/userId/queue/messages} : Destination of sending message. In this endpoint client listen messages.
     *
     * @param message   The body of sending messages. It is representation for ConversationMessage entity.
     * @param principal Authenticated user
     * @see pl.chatme.config.WebSocketConfiguration
     */
    @MessageMapping("/chat")
    public void processMessage(@Payload ConversationMessageDTO message, Principal principal) {

        var conversation = conversationService.getConversation(principal.getName(), message.getRecipient().getId());
        var conversationMessage = conversationMessageService.saveConversationMessage(conversation.getId(), message.getContent(), message.getTime());

        simpMessagingTemplate.convertAndSendToUser(conversationMessage.getRecipient().getId().toString(),
                "/queue/messages", conversationMessage);

    }
}
