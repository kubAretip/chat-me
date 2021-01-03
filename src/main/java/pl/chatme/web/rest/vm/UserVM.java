package pl.chatme.web.rest.vm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.chatme.service.dto.UserDTO;

import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
public class UserVM extends UserDTO {

    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int PASSWORD_MAX_LENGTH = 32;

    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    private String password;

}
