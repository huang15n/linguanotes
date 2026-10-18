package com.linguanotes.config;

import com.linguanotes.model.Language;
import com.linguanotes.model.AccountRole;
import com.linguanotes.model.UserAccount;
import com.linguanotes.repository.LanguageRepository;
import com.linguanotes.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Locale;

@Configuration
public class DataInitializer {
    @Bean CommandLineRunner seed(LanguageRepository repo, UserAccountRepository accounts,
                                 PasswordEncoder passwords,
                                 @Value("${app.bootstrap-admin.email:}") String adminEmail,
                                 @Value("${app.bootstrap-admin.password:}") String adminPassword) {
        return args -> {
            seedLanguage(repo, "Español", "es");
            seedLanguage(repo, "Français", "fr");
            seedLanguage(repo, "English", "en");
            seedLanguage(repo, "简体中文", "zh-CN");
            if (adminEmail.isBlank() && adminPassword.isBlank()) return;
            if (adminEmail.isBlank() || adminPassword.length() < 12) {
                throw new IllegalStateException("Bootstrap admin requires an email and a password of at least 12 characters");
            }
            String normalizedEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
            if (accounts.existsByEmail(normalizedEmail)) return;
            UserAccount admin = new UserAccount();
            admin.setName("LinguaNotes Admin");
            admin.setEmail(normalizedEmail);
            admin.setPasswordHash(passwords.encode(adminPassword));
            admin.setRole(AccountRole.ADMIN);
            accounts.save(admin);
        };
    }

    private void seedLanguage(LanguageRepository repo, String name, String code) {
        if (repo.findByCodeIgnoreCase(code).isEmpty()) {
            repo.save(new Language(null, name, code));
        }
    }
}
