package pl.chatme.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.chatme.domain.User;

import java.util.Collection;
import java.util.stream.Collectors;

public class SecurityUserDetails implements UserDetails {

    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Long userId;

    public SecurityUserDetails(long userId, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        this.password = password;
        this.username = username;
        this.userId = userId;
        this.authorities = authorities;
    }

    public SecurityUserDetails(User user) {
        this.password = user.getPassword();
        this.username = user.getLogin();
        this.userId = user.getId();
        this.authorities = user.getAuthorities()
                .stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                .collect(Collectors.toList());
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
