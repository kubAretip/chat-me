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
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.error.MissingEnvironmentVariableException;
import pl.chatme.security.SecurityUserDetails;

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
    private static final String SUB_ID_KEY = "subId";
    private static final String TOKEN_TYPE_KEY = "typ";
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

        var user = (SecurityUserDetails) authentication.getPrincipal();
        var authorities = convertUserAuthorityToStringList(user);

        var expiresAt = new Date(System.currentTimeMillis() + this.tokenValidityInMilliseconds);

        return JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(expiresAt)
                .withClaim(SUB_ID_KEY, user.getUserId())
                .withClaim(AUTHORITIES_KEY, authorities)
                .withClaim(TOKEN_TYPE_KEY, TOKEN_PREFIX.replace(" ", ""))
                .sign(jwtAlgorithm);
    }

    public DecodedJWT decodeToken(String token) {
        try {
            return JWT.require(jwtAlgorithm)
                    .build()
                    .verify(token);
        } catch (JWTDecodeException exception) {
            throw new RuntimeException(exception.getLocalizedMessage());
        }
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
        if (tokenString != null) {

            var decodedToken = decodeToken(tokenString);

            if (decodedToken != null) {
                var authorities = getAuthoritiesFromDecodedJWT(decodedToken);
                var subject = decodedToken.getSubject();
                var subjectId = decodedToken.getClaim(SUB_ID_KEY).asLong();

                if (subject != null) {
                    var user = new SecurityUserDetails(subjectId, subject, "", authorities);
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
            throw new RuntimeException("Value of authorization header is empty or not contains '" + TOKEN_PREFIX + "' prefix.");
    }


    private List<String> convertUserAuthorityToStringList(SecurityUserDetails user) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

}
