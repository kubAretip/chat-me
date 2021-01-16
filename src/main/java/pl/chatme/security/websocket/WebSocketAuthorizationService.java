package pl.chatme.security.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import pl.chatme.security.jwt.TokenProvider;

@Slf4j
@Component
public class WebSocketAuthorizationService {

    private final TokenProvider tokenProvider;

    public WebSocketAuthorizationService(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    public UsernamePasswordAuthenticationToken attemptAuthentication(String authorizationHeaderValue) {
        var token = tokenProvider.extractToken(authorizationHeaderValue);
        return tokenProvider.getAuthentication(token);
    }

}
