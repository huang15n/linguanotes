package com.linguanotes.service;

import com.linguanotes.dto.PreferenceRequest;
import com.linguanotes.dto.UserPreferenceResponse;
import com.linguanotes.model.UserPreference;
import com.linguanotes.repository.UserAccountRepository;
import com.linguanotes.repository.UserPreferenceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Set;

@Service
@Transactional
public class PreferenceService {
    private static final Set<String> LOCALES = Set.of("fr", "es", "en", "zh-CN");
    private final UserPreferenceRepository preferences;
    private final UserAccountRepository accounts;

    public PreferenceService(UserPreferenceRepository preferences, UserAccountRepository accounts) {
        this.preferences = preferences;
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public UserPreferenceResponse get(Long userId) {
        return preferences.findByUserId(userId).map(this::response)
            .orElse(new UserPreferenceResponse("fr", 15, false));
    }

    public UserPreferenceResponse update(Long userId, PreferenceRequest request) {
        if (!LOCALES.contains(request.interfaceLocale())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Langue d’interface non prise en charge");
        }
        UserPreference preference = preferences.findByUserId(userId).orElseGet(() -> {
            UserPreference created = new UserPreference();
            created.setUser(accounts.getReferenceById(userId));
            return created;
        });
        preference.setInterfaceLocale(request.interfaceLocale());
        if (request.dailyGoalMinutes() != null) preference.setDailyGoalMinutes(request.dailyGoalMinutes());
        return response(preferences.save(preference));
    }

    private UserPreferenceResponse response(UserPreference preference) {
        return new UserPreferenceResponse(preference.getInterfaceLocale(), preference.getDailyGoalMinutes(), true);
    }
}