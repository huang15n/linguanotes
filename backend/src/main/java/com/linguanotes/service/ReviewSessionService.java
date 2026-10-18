package com.linguanotes.service;

import com.linguanotes.dto.ReviewSessionItemRequest;
import com.linguanotes.dto.ReviewSessionRequest;
import com.linguanotes.dto.ReviewSessionResponse;
import com.linguanotes.model.ReviewSession;
import com.linguanotes.model.ReviewSessionItem;
import com.linguanotes.model.VocabularyEntry;
import com.linguanotes.repository.LanguageRepository;
import com.linguanotes.repository.ReviewSessionItemRepository;
import com.linguanotes.repository.ReviewSessionRepository;
import com.linguanotes.repository.UserAccountRepository;
import com.linguanotes.repository.VocabularyEntryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ReviewSessionService {
    private final ReviewSessionRepository sessions;
    private final ReviewSessionItemRepository items;
    private final VocabularyEntryRepository vocabulary;
    private final UserAccountRepository accounts;
    private final LanguageRepository languages;

    public ReviewSessionService(ReviewSessionRepository sessions, ReviewSessionItemRepository items,
                                VocabularyEntryRepository vocabulary, UserAccountRepository accounts,
                                LanguageRepository languages) {
        this.sessions = sessions;
        this.items = items;
        this.vocabulary = vocabulary;
        this.accounts = accounts;
        this.languages = languages;
    }

    public ReviewSessionResponse create(Long userId, ReviewSessionRequest request) {
        List<VocabularyEntry> entries = new ArrayList<>();
        for (ReviewSessionItemRequest item : request.items()) {
            entries.add(vocabulary.findByIdAndOwnerId(item.vocabularyEntryId(), userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Fiche de vocabulaire introuvable")));
        }
        int correct = (int) request.items().stream().filter(ReviewSessionItemRequest::correct).count();
        ReviewSession session = new ReviewSession();
        session.setUser(accounts.getReferenceById(userId));
        session.setLanguage(request.languageId() == null ? null
                : languages.findById(request.languageId()).orElse(null));
        session.setMode(request.mode());
        session.setTotalWords(entries.size());
        session.setCorrectCount(correct);
        session.setIncorrectCount(entries.size() - correct);
        session.setStartedAt(LocalDateTime.now().minusSeconds(entries.size() * 5L));
        session.setCompletedAt(LocalDateTime.now());
        session = sessions.saveAndFlush(session);

        for (int index = 0; index < entries.size(); index++) {
            ReviewSessionItem item = new ReviewSessionItem();
            item.setSession(session);
            item.setVocabularyEntry(entries.get(index));
            item.setCorrect(request.items().get(index).correct());
            item.setAnsweredAt(session.getCompletedAt());
            items.save(item);
        }
        return response(session);
    }

    @Transactional(readOnly = true)
    public List<ReviewSessionResponse> list(Long userId) {
        return sessions.findAllByUserIdOrderByStartedAtDesc(userId).stream()
                .map(this::response)
                .toList();
    }

    private ReviewSessionResponse response(ReviewSession session) {
        return new ReviewSessionResponse(session.getId(), session.getMode(),
                session.getLanguage() == null ? null : session.getLanguage().getId(),
                session.getTotalWords(), session.getCorrectCount(), session.getIncorrectCount(),
                session.getStartedAt(), session.getCompletedAt());
    }
}
