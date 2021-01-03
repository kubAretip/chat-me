package pl.chatme.security.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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


        var tokenStringFromRequest = tokenProvider.extractTokenStringFromRequest(request);

        if (tokenStringFromRequest != null) {
            var authenticationToken = getAuthentication(tokenStringFromRequest);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        chain.doFilter(request, response);

    }


    // Reads the JWT from the authorization header and validate the token
    private UsernamePasswordAuthenticationToken getAuthentication(String tokenString) {

        if (tokenString != null) {

            var decodedToken = tokenProvider.decodeToken(tokenString);

            if (decodedToken != null) {
                var authorities = tokenProvider.getAuthoritiesFromDecodedJWT(decodedToken);
                var subject = decodedToken.getSubject();

                if (subject != null) {
                    var user = new User(subject, "", authorities);
                    return new UsernamePasswordAuthenticationToken(user, tokenString, authorities);
                }
            }

        }
        return null;
    }


}
