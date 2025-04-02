package com.shopsavvy.shopshavvy.security;

import com.shopsavvy.shopshavvy.model.users.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UserDetailsImpl implements UserDetails {

    private User user;
    public UserDetailsImpl(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.isExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.isCredentialsExpired();
    }

    @Override
    public boolean isEnabled() {
        return user.getIsActive();
    }
}