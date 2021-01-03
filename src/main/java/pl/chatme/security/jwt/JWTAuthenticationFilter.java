package pl.chatme.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;

    public JWTAuthenticationFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        try {
            var authRequest = objectMapper.readValue(request.getInputStream(), AuthenticationRequest.class);

            var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                    authRequest.getLogin(), authRequest.getPassword());

            return this.getAuthenticationManager().authenticate(usernamePasswordAuthenticationToken);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
