package pl.chatme.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.chatme.service.ChatMessageService;
import pl.chatme.service.dto.ChatMessageDTO;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/messages")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @GetMapping(params = {"first_user_id", "second_user_id", "size"})
    public ResponseEntity<List<ChatMessageDTO>> getMessageWithSize(@RequestParam("first_user_id") long firstUserId,
                                                                   @RequestParam("second_user_id") long secondUserId,
                                                                   @RequestParam("size") int size) {
        return ResponseEntity.ok(chatMessageService.getMessagesWithSize(firstUserId, secondUserId, size));
    }

    @GetMapping(params = {"first_user_id", "second_user_id", "size", "before_time"})
    public ResponseEntity<List<ChatMessageDTO>> getOneToOneConversation(@RequestParam("first_user_id") long firstUserId,
                                                                        @RequestParam("second_user_id") long secondUserId,
                                                                        @RequestParam("size") int size,
                                                                        @RequestParam("before_time") String beforeTime) {

        return ResponseEntity.ok(chatMessageService.getMessagesWithSizeAndBeforeTime(firstUserId, secondUserId, beforeTime, size));
    }


}
