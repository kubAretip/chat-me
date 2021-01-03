package pl.chatme.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Object to return as body in case failure authentication.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class AuthenticationFailureVM {

    @JsonProperty("error")
    private String error;

    @JsonProperty("error_description")
    private String errorDescription;
}
