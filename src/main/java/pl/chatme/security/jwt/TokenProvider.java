package pl.chatme.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.error.MissingEnvironmentVariableException;

import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static pl.chatme.config.Constants.TOKEN_PREFIX;

@Slf4j
@Component
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "roles";

    private final long tokenValidityInMilliseconds;
    private final Algorithm jwtAlgorithm;

    // default validity time is 15 minutes
    public TokenProvider(@Value("${jwt.base64-secret}") String base64Secret,
                         @Value("${jwt.validity-time-in-seconds:900}") Long validityTimeInSeconds) {

        if (Strings.isNullOrEmpty(base64Secret)) {
            throw new MissingEnvironmentVariableException("Secret is empty or null.");
        }

        if (!org.apache.commons.codec.binary.Base64.isBase64(base64Secret)) {
            throw new IllegalArgumentException("Secret is not Base64.");
        }

        this.tokenValidityInMilliseconds = validityTimeInSeconds * 1000;
        byte[] keyByte = Base64.getDecoder().decode(base64Secret);
        this.jwtAlgorithm = Algorithm.HMAC512(keyByte);
    }

    public String createToken(Authentication authentication) {

        var user = (User) authentication.getPrincipal();
        var authorities = convertUserAuthorityToStringList(user);

        var expiresAt = new Date(System.currentTimeMillis() + this.tokenValidityInMilliseconds);

        return JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(expiresAt)
                .withClaim(AUTHORITIES_KEY, authorities)
                .sign(jwtAlgorithm);
    }

    public DecodedJWT decodeToken(String token) {
        log.debug("Decode token: {}", token);
        try {
            return JWT.require(jwtAlgorithm)
                    .build()
                    .verify(token);
        } catch (JWTDecodeException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public TokenVM createTokenVMResponse(String token) {
        return new TokenVM(token, TOKEN_PREFIX.replace(" ", "").toLowerCase());
    }

    public Collection<? extends GrantedAuthority> getAuthoritiesFromDecodedJWT(DecodedJWT decodedToken) {
        return decodedToken
                .getClaim(AUTHORITIES_KEY)
                .asList(String.class)
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    // Reads the JWT from the authorization header and validate the token
    public UsernamePasswordAuthenticationToken getAuthentication(String tokenString) {
        log.debug("Getting authentication from token {}", tokenString);
        if (tokenString != null) {

            var decodedToken = decodeToken(tokenString);

            if (decodedToken != null) {
                var authorities = getAuthoritiesFromDecodedJWT(decodedToken);
                var subject = decodedToken.getSubject();

                if (subject != null) {
                    var user = new User(subject, "", authorities);
                    return new UsernamePasswordAuthenticationToken(user, tokenString, authorities);
                }
            }
        }
        return null;
    }

    public Boolean isValidTokenRequestHeader(String authorizationHeader) {
        return !Strings.isNullOrEmpty(authorizationHeader) && authorizationHeader.startsWith(TOKEN_PREFIX);
    }

    public String extractToken(String authHeaderValue) {

        if (isValidTokenRequestHeader(authHeaderValue)) {
            return authHeaderValue.replace(TOKEN_PREFIX, "");
        } else
            throw new RuntimeException("Value of authorization header is empty or not contains Bearer prefix.");
    }


    private List<String> convertUserAuthorityToStringList(User user) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

}
