package pl.chatme.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.chatme.config.Constants;
import pl.chatme.domain.User;

import javax.validation.constraints.*;

/**
 * A DTO representing a {@link User}.
 */
@Setter
@Getter
@NoArgsConstructor
public class UserDTO {

    private Long id = null;

    @NotBlank
    @Size(min = 4, max = 50)
    @Pattern(regexp = Constants.LOGIN_REGEX)
    private String login;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    private String email;

    @NotNull
    private Boolean activated = false;

    private String friendRequestCode;
}
