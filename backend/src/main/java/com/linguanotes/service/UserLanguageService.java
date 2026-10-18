package com.linguanotes.service;

import com.linguanotes.dto.UserLanguageRequest;
import com.linguanotes.dto.UserLanguageResponse;
import com.linguanotes.model.UserLanguage;
import com.linguanotes.repository.LanguageRepository;
import com.linguanotes.repository.UserAccountRepository;
import com.linguanotes.repository.UserLanguageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class UserLanguageService {
    private final UserLanguageRepository entries;
    private final LanguageRepository languages;
    private final UserAccountRepository accounts;

    public UserLanguageService(UserLanguageRepository entries, LanguageRepository languages,
                               UserAccountRepository accounts) {
        this.entries = entries;
        this.languages = languages;
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public List<UserLanguageResponse> list(Long userId) {
        return entries.findAllByUserIdOrderByCreatedAtAsc(userId).stream().map(this::map).toList();
    }

    public UserLanguageResponse save(Long userId, UserLanguageRequest request) {
        var language = languages.findById(request.languageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Langue introuvable"));
        UserLanguage entry = entries.findByUserIdAndLanguageId(userId, request.languageId())
                .orElseGet(() -> {
                    UserLanguage created = new UserLanguage();
                    created.setUser(accounts.getReferenceById(userId));
                    created.setLanguage(language);
                    return created;
                });
        entry.setLevel(request.level());
        entry.setTarget(request.target() == null || request.target());
        return map(entries.save(entry));
    }

    public void delete(Long userId, Long id) {
        entries.findByIdAndUserId(id, userId)
                .ifPresentOrElse(entries::delete, () -> {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Langue introuvable");
                });
    }

    private UserLanguageResponse map(UserLanguage entry) {
        return new UserLanguageResponse(entry.getId(), entry.getLanguage().getId(),
                entry.getLanguage().getName(), entry.getLevel(), entry.isTarget());
    }
}
