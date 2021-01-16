package pl.chatme.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ChatMessageDTO {

    private Long id;
    private UserDTO sender;
    private UserDTO recipient;
    private String content;
    private String time;
    private String messageStatus;

}
