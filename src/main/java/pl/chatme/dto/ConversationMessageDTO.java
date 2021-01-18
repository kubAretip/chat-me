package pl.chatme.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ConversationMessageDTO {

    private Long id;
    private Long conversationId;
    private UserDTO sender;
    private UserDTO recipient;
    private String content;
    private String time;
    private String messageStatus;

}
