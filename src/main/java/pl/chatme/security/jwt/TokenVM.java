package pl.chatme.security.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Object to return as body in authentication
 */
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
class TokenVM {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;
}
