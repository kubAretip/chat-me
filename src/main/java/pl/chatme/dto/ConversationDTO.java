package pl.chatme.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConversationDTO {

    private Long id;
    private Long conversationWithId;
    private UserDTO recipient;

}
