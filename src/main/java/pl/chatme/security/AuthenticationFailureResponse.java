package pl.chatme.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Object to return as body in case failure authentication.
 */
@NoArgsConstructor
final class AuthenticationFailureResponse {

    @JsonProperty("title")
    private final String title = "Unsuccessful login";

    @JsonProperty("status")
    private final int status = HttpStatus.UNAUTHORIZED.value();

    @JsonProperty("details")
    private String details = "Invalid login or password.";

    public AuthenticationFailureResponse(String details) {
        this.details = details;
    }
}
