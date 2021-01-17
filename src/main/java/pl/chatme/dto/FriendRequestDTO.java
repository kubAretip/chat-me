package pl.chatme.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class FriendRequestDTO {

    private Long id;
    private UserDTO sender;
    private UserDTO recipient;
    public String sentTime;
    private String status;

}
