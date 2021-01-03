package pl.chatme.security;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public AuthenticationSuccessHandler(TokenProvider tokenProvider,
                                        ObjectMapper objectMapper) {
        this.tokenProvider = tokenProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        var out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        var token = tokenProvider.createToken(authentication);
        var tokenJson = objectMapper.writeValueAsString(tokenProvider.createTokenVMResponse(token));

        out.print(tokenJson);
        out.flush();
    }
}
