package pl.chatme.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import pl.chatme.dto.ConversationDTO;
import pl.chatme.dto.mapper.ConversationMapper;
import pl.chatme.service.ConversationService;
import pl.chatme.service.exception.NotFoundException;

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
        try {
            return ResponseEntity.ok(conversationService.getSenderConversation(principal.getName())
                    .stream()
                    .map(conversationMapper::mapToConversationDTO)
                    .collect(Collectors.toList()));
        } catch (NotFoundException ex) {
            throw Problem.builder()
                    .withStatus(Status.NOT_FOUND)
                    .withTitle(ex.getTitle())
                    .withDetail(ex.getLocalizedMessage())
                    .build();
        }
    }


}
