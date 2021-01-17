package pl.chatme.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception is thrown when not activated user trying to authenticate.
 */
public class UserNotActivatedException extends AuthenticationException {
    public UserNotActivatedException(String msg) {
        super(msg);
    }
}
