package com.linguanotes.service;

import com.linguanotes.dto.Dto.*;
import com.linguanotes.model.AccountRole;
import com.linguanotes.model.PasswordResetToken;
import com.linguanotes.model.UserAccount;
import com.linguanotes.repository.PasswordResetTokenRepository;
import com.linguanotes.repository.UserAccountRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

@Service
public class AccountService {
    private final UserAccountRepository accounts;
    private final PasswordResetTokenRepository resetTokens;
    private final PasswordEncoder passwords;
    private final ObjectProvider<JavaMailSender> mailSenders;
    private final FileStorageService fileStorage;
    private final SecureRandom random = new SecureRandom();

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${app.mail.from:noreply@linguanotes.local}")
    private String mailFrom;

    @Value("${app.public-url:http://localhost:4200}")
    private String publicUrl;

    public AccountService(UserAccountRepository accounts, PasswordResetTokenRepository resetTokens,
                          PasswordEncoder passwords, ObjectProvider<JavaMailSender> mailSenders,
                          FileStorageService fileStorage) {
        this.accounts = accounts;
        this.resetTokens = resetTokens;
        this.passwords = passwords;
        this.mailSenders = mailSenders;
        this.fileStorage = fileStorage;
    }

    @Transactional
    public AccountResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (accounts.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette adresse électronique est déjà utilisée");
        }
        UserAccount account = new UserAccount();
        account.setName(request.name().trim());
        account.setEmail(email);
        account.setPasswordHash(passwords.encode(request.password()));
        account.setRole(AccountRole.USER);
        return response(accounts.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long id) {
        return response(accounts.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expirée")));
    }

    @Transactional
    public AccountResponse updateProfile(Long id, ProfileUpdateRequest request) {
        UserAccount account = findAccount(id);
        account.setName(request.name().trim());
        return response(accounts.save(account));
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        UserAccount account = findAccount(id);
        if (!passwords.matches(request.currentPassword(), account.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mot de passe actuel incorrect");
        }
        account.setPasswordHash(passwords.encode(request.newPassword()));
        resetTokens.deleteByUserId(id);
        accounts.save(account);
    }

    @Transactional
    public void deleteAccount(Long id, DeleteAccountRequest request) {
        UserAccount account = findAccount(id);
        if (!passwords.matches(request.password(), account.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mot de passe incorrect");
        }
        resetTokens.deleteByUserId(id);
        fileStorage.deleteAllByOwner(id);
        accounts.delete(account);
    }

    @Transactional
    public AccountResponse updateRole(Long id, AccountRole role) {
        UserAccount account = findAccount(id);
        account.setRole(role);
        return response(accounts.save(account));
    }

    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        if (mailHost.isBlank() || mailSenders.getIfAvailable() == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "La réinitialisation par courriel n’est pas configurée côté serveur");
        }
        Optional<UserAccount> found = accounts.findByEmail(normalizeEmail(request.email()));
        if (found.isEmpty()) return;

        UserAccount account = found.get();
        resetTokens.deleteByUserId(account.getId());
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        PasswordResetToken token = new PasswordResetToken();
        token.setTokenHash(hash(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        token.setUser(account);
        resetTokens.save(token);

        if (mailHost.isBlank()) return;
        try {
            sendResetEmail(mailSenders.getObject(), account, rawToken);
        } catch (MailException | MessagingException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Le service de courrier électronique est temporairement indisponible");
        }
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        PasswordResetToken token = resetTokens.findByTokenHashAndExpiresAtAfter(
                        hash(request.token()), LocalDateTime.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Le lien de réinitialisation est invalide ou expiré"));
        UserAccount account = token.getUser();
        account.setPasswordHash(passwords.encode(request.password()));
        accounts.save(account);
        resetTokens.deleteByUserId(account.getId());
    }

    private void sendResetEmail(JavaMailSender sender, UserAccount account, String token)
            throws MessagingException {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
        helper.setFrom(mailFrom);
        helper.setTo(account.getEmail());
        helper.setSubject("Réinitialisation du mot de passe LinguaNotes");
        helper.setText("Pour choisir un nouveau mot de passe, ouvrez ce lien :\n\n" +
                publicUrl + "/auth/reset?token=" + token + "\n\n" +
                "Ce lien expire dans 30 minutes.", false);
        sender.send(message);
    }

    private UserAccount findAccount(Long id) {
        return accounts.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expirée"));
    }

    private AccountResponse response(UserAccount account) {
        return new AccountResponse(account.getId(), account.getName(), account.getEmail(), account.getRole().name());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}