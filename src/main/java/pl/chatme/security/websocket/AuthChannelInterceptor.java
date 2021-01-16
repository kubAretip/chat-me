package pl.chatme.security.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import pl.chatme.config.Constants;

@Slf4j
@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final WebSocketAuthorizationService webSocketAuthService;

    public AuthChannelInterceptor(WebSocketAuthorizationService webSocketAuthService) {
        this.webSocketAuthService = webSocketAuthService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            var authorizationHeaderValue = (String) accessor.getFirstNativeHeader(Constants.TOKEN_HEADER);
            final var authenticationToken = webSocketAuthService.attemptAuthentication(authorizationHeaderValue);

            if (authenticationToken != null)
                accessor.setUser(authenticationToken);
        }
        return message;
    }
}
