package pl.chatme.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.chatme.dto.ConversationDTO;
import pl.chatme.dto.mapper.ConversationMapper;
import pl.chatme.service.ConversationService;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final ConversationMapper conversationMapper;


    public ConversationController(ConversationService conversationService,
                                  ConversationMapper conversationMapper) {
        this.conversationService = conversationService;
        this.conversationMapper = conversationMapper;
    }

    @GetMapping
    public ResponseEntity<List<ConversationDTO>> getUserConversation(Principal principal) {

        return ResponseEntity.ok(conversationService.getSenderConversation(principal.getName())
                .stream()
                .map(conversationMapper::mapToConversationDTO)
                .collect(Collectors.toList()));
    }


}
