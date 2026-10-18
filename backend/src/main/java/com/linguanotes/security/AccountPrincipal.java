package com.linguanotes.security;

import com.linguanotes.model.AccountRole;
import com.linguanotes.model.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

public record AccountPrincipal(Long id, String name, String email, String passwordHash,
                               AccountRole role, boolean userEnabled) implements UserDetails {
    public static AccountPrincipal from(UserAccount account) {
        return new AccountPrincipal(account.getId(), account.getName(), account.getEmail(),
                account.getPasswordHash(), account.getRole(), account.isEnabled());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return userEnabled; }
}