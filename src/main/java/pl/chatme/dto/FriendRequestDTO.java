package pl.chatme.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.chatme.domain.FriendRequest;

/**
 * A DTO representing a {@link FriendRequest}.
 */
@NoArgsConstructor
@Getter
@Setter
public class FriendRequestDTO {

    public String sentTime;
    private Long id;
    private UserDTO sender;
    private UserDTO recipient;
    private String status;

}
