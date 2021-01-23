package pl.chatme.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.chatme.domain.Conversation;

/**
 * A DTO representing a {@link Conversation}.
 */
@Getter
@Setter
@NoArgsConstructor
public class ConversationDTO {

    private Long id;
    private Long conversationWithId;
    private UserDTO recipient;

}
