package pl.chatme.web.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.chatme.domain.ChatMessage;
import pl.chatme.service.ChatMessageService;
import pl.chatme.service.ConversationService;
import pl.chatme.service.dto.ChatMessageDTO;
import pl.chatme.web.rest.vm.MessageVM;
import pl.chatme.web.rest.vm.SimpleMessageVM;

import java.util.List;

@Slf4j
@RestController
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ConversationService conversationService;
    private final ChatMessageService chatMessageService;

    public ChatController(SimpMessagingTemplate simpMessagingTemplate,
                          ConversationService conversationService,
                          ChatMessageService chatMessageService) {

        this.simpMessagingTemplate = simpMessagingTemplate;
        this.conversationService = conversationService;
        this.chatMessageService = chatMessageService;
    }

    /**
     * To send message use url /app/chat and add MessageVM body.
     * In front end part to subscribe message use url /user/{senderId}/queue/messages
     *
     * @param messageVM
     */
    @MessageMapping("/chat")
    public ResponseEntity<String> processMessage(@Payload MessageVM messageVM) {
        conversationService.getConversation(messageVM.getSenderId(), messageVM.getRecipientId())
                .ifPresent(conversation -> {

                    chatMessageService.saveChatMessage(conversation, messageVM.getContent(), messageVM.getTime());
                    simpMessagingTemplate.convertAndSendToUser(conversation.getRecipient().getId().toString(),
                            "/queue/messages", new SimpleMessageVM(conversation.getSender().getLogin(), messageVM.getContent()));
                });
        return ResponseEntity.ok("CONNECTED");
    }
}
