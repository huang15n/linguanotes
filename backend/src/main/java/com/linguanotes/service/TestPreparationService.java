package com.linguanotes.service;

import com.linguanotes.dto.TestPreparationAutosaveRequest;
import com.linguanotes.dto.TestPreparationRequest;
import com.linguanotes.dto.TestPreparationResponse;
import com.linguanotes.model.*;
import com.linguanotes.repository.StoredFileRepository;
import com.linguanotes.repository.TestPreparationRepository;
import com.linguanotes.repository.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class TestPreparationService {

    private static final Map<ExamLanguage, Set<ExamType>> TYPES_BY_LANGUAGE = Map.of(
            ExamLanguage.ENGLISH, EnumSet.of(ExamType.IELTS),
            ExamLanguage.FRENCH, EnumSet.of(ExamType.TCF, ExamType.TEF, ExamType.DELF),
            ExamLanguage.SPANISH, EnumSet.of(ExamType.DELE)
    );

    private static final Set<String> CEFR_LEVELS = Set.of("A1", "A2", "B1", "B2", "C1", "C2");
    private static final Set<String> IELTS_LEVELS = Set.of("6", "6.5", "7", "7.5", "8", "8.5", "9",
            "IELTS_6", "IELTS_7", "IELTS_8", "IELTS_9");

    private final TestPreparationRepository repository;
    private final UserAccountRepository accounts;
    private final StoredFileRepository files;

    public TestPreparationService(TestPreparationRepository repository,
                                  UserAccountRepository accounts,
                                  StoredFileRepository files) {
        this.repository = repository;
        this.accounts = accounts;
        this.files = files;
    }

    @Transactional(readOnly = true)
    public List<TestPreparationResponse> search(Long viewerId,
                                                boolean isAdmin,
                                                String q,
                                                ExamLanguage examLanguage,
                                                ExamType examType,
                                                String examLevel,
                                                ExamSkillModule skillModule,
                                                Boolean favorite,
                                                PublicationStatus status,
                                                boolean mineOnly) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        return repository.searchVisible(viewerId, isAdmin, query, examLanguage, examType, examLevel,
                        skillModule, favorite, status, mineOnly)
                .stream()
                .map(t -> toResponse(t, viewerId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TestPreparationResponse> recentOwned(Long ownerId, int limit) {
        return repository.findTop5ByOwnerIdOrderByUpdatedAtDesc(ownerId).stream()
                .limit(limit)
                .map(t -> toResponse(t, ownerId))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> countByModule(Long ownerId) {
        return repository.searchVisible(ownerId, false, null, null, null, null, null, null, null, true)
                .stream()
                .collect(Collectors.groupingBy(t -> t.getSkillModule().name(), Collectors.counting()));
    }

    @Transactional(readOnly = true)
    public long countOwned(Long ownerId) {
        return repository.countByOwnerId(ownerId);
    }

    @Transactional(readOnly = true)
    public long countFavorites(Long ownerId) {
        return repository.countByOwnerIdAndFavoriteTrue(ownerId);
    }

    @Transactional(readOnly = true)
    public TestPreparationResponse get(Long id, Long viewerId, boolean isAdmin) {
        TestPreparation entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Préparation introuvable"));
        assertCanView(entity, viewerId, isAdmin);
        return toResponse(entity, viewerId);
    }

    public TestPreparationResponse create(TestPreparationRequest request, Long ownerId) {
        validateExamMetadata(request.examLanguage(), request.examType(), request.examLevel());
        UserAccount owner = accounts.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        TestPreparation entity = new TestPreparation();
        entity.setOwner(owner);
        applyRequest(entity, request);
        return toResponse(repository.save(entity), ownerId);
    }

    public TestPreparationResponse update(Long id, TestPreparationRequest request, Long ownerId, boolean isAdmin) {
        validateExamMetadata(request.examLanguage(), request.examType(), request.examLevel());
        TestPreparation entity = requireEditable(id, ownerId, isAdmin);
        applyRequest(entity, request);
        return toResponse(repository.save(entity), ownerId);
    }

    public TestPreparationResponse autosave(Long id, TestPreparationAutosaveRequest request, Long ownerId, boolean isAdmin) {
        TestPreparation entity = requireEditable(id, ownerId, isAdmin);
        if (entity.getVersion() != request.version()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Version conflict: the preparation was modified elsewhere");
        }
        entity.setContent(request.content());
        return toResponse(repository.save(entity), ownerId);
    }

    public void delete(Long id, Long ownerId, boolean isAdmin) {
        TestPreparation entity = requireEditable(id, ownerId, isAdmin);
        repository.delete(entity);
    }

    private void applyRequest(TestPreparation entity, TestPreparationRequest request) {
        entity.setTitle(request.title().trim());
        entity.setContent(request.content());
        entity.setExamLanguage(request.examLanguage());
        entity.setExamType(request.examType());
        entity.setExamLevel(normalizeLevel(request.examType(), request.examLevel()));
        entity.setSkillModule(request.skillModule());
        entity.setPublicationStatus(
                request.publicationStatus() != null ? request.publicationStatus() : PublicationStatus.DRAFT);
        // visibility privée par défaut (héritage historique) — non exposée au flux publish
        if (entity.getVisibility() == null) {
            entity.setVisibility(NoteVisibility.PRIVATE);
        }
        if (request.favorite() != null) {
            entity.setFavorite(request.favorite());
        }
        if (request.audioFileId() != null) {
            StoredFile file = files.findById(request.audioFileId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichier audio introuvable"));
            entity.setAudioFile(file);
        } else {
            entity.setAudioFile(null);
        }
    }

    private void validateExamMetadata(ExamLanguage language, ExamType type, String level) {
        Set<ExamType> allowed = TYPES_BY_LANGUAGE.get(language);
        if (allowed == null || !allowed.contains(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le type d'examen " + type + " n'est pas valide pour la langue " + language);
        }
        String normalized = normalizeLevel(type, level);
        if (type == ExamType.IELTS) {
            if (!IELTS_LEVELS.contains(normalized) && !IELTS_LEVELS.contains(level.trim())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Niveau IELTS invalide (attendu 6–9)");
            }
        } else if (!CEFR_LEVELS.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Niveau CECRL invalide (A1–C2)");
        }
    }

    private String normalizeLevel(ExamType type, String level) {
        String raw = level == null ? "" : level.trim().toUpperCase().replace(' ', '_');
        if (type == ExamType.IELTS) {
            if (raw.startsWith("IELTS_")) {
                return raw.substring("IELTS_".length());
            }
            return raw;
        }
        return raw;
    }

    private void assertCanView(TestPreparation entity, Long viewerId, boolean isAdmin) {
        if (entity.getPublicationStatus() == PublicationStatus.PUBLISHED) {
            return;
        }
        if (isAdmin || entity.getOwner().getId().equals(viewerId)) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Préparation introuvable");
    }

    private TestPreparation requireEditable(Long id, Long ownerId, boolean isAdmin) {
        TestPreparation entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Préparation introuvable"));
        if (!isAdmin && !entity.getOwner().getId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Modification non autorisée");
        }
        return entity;
    }

    private TestPreparationResponse toResponse(TestPreparation t, Long viewerId) {
        StoredFile audio = t.getAudioFile();
        String authorName = t.getOwner().getDisplayName() != null && !t.getOwner().getDisplayName().isBlank()
                ? t.getOwner().getDisplayName()
                : t.getOwner().getName();
        return new TestPreparationResponse(
                t.getId(),
                t.getTitle(),
                t.getContent(),
                t.getExamLanguage(),
                t.getExamType(),
                t.getExamLevel(),
                t.getSkillModule(),
                t.getPublicationStatus(),
                t.isFavorite(),
                t.getVersion(),
                t.getLastSavedAt(),
                t.getCreatedAt(),
                t.getUpdatedAt(),
                t.getOwner().getId(),
                authorName,
                audio != null ? audio.getId() : null,
                audio != null ? audio.getOriginalName() : null,
                viewerId != null && viewerId.equals(t.getOwner().getId())
        );
    }
}
