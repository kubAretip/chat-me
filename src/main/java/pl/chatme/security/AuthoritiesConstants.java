package pl.chatme.security;

import lombok.Getter;

public enum AuthoritiesConstants {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),
    ANONYMOUS("ROLE_ANONYMOUS"),
    MODERATOR("ROLE_MODERATOR");

    @Getter
    private final String role;

    AuthoritiesConstants(String role) {
        this.role = role;
    }
}
