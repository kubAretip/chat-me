package pl.chatme.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pl.chatme.security.jwt.TokenProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;

    public AuthenticationSuccessHandler(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        var token = tokenProvider.createToken(authentication);

        response.getOutputStream().print(token);
    }
}
