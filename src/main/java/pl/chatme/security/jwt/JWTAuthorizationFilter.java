package pl.chatme.security.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static pl.chatme.config.Constants.TOKEN_HEADER;

@Slf4j
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    private final TokenProvider tokenProvider;

    public JWTAuthorizationFilter(AuthenticationManager authenticationManager,
                                  TokenProvider tokenProvider) {
        super(authenticationManager);
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        var authorizationHeaderValue = request.getHeader(TOKEN_HEADER);
        try {
            var token = tokenProvider.extractToken(authorizationHeaderValue);

            if (token != null) {
                var authenticationToken = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (RuntimeException ex) {
            log.debug(ex.getLocalizedMessage());
        }

        chain.doFilter(request, response);
    }
}
