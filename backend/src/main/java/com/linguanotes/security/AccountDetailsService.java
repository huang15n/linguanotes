package com.linguanotes.security;

import com.linguanotes.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountDetailsService implements UserDetailsService {
    private final UserAccountRepository accounts;

    public AccountDetailsService(UserAccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return accounts.findByEmail(email.trim().toLowerCase())
                .map(AccountPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("Compte introuvable"));
    }
}