package com.linguanotes.service;

import com.linguanotes.dto.NoteAutosaveRequest;
import com.linguanotes.dto.NoteRequest;
import com.linguanotes.dto.NoteResponse;
import com.linguanotes.model.Language;
import com.linguanotes.model.Lesson;
import com.linguanotes.model.Note;
import com.linguanotes.model.NoteType;
import com.linguanotes.model.NoteVisibility;
import com.linguanotes.model.Tag;
import com.linguanotes.repository.LanguageRepository;
import com.linguanotes.repository.LessonRepository;
import com.linguanotes.repository.NoteCommentRepository;
import com.linguanotes.repository.NoteLikeRepository;
import com.linguanotes.repository.NoteRepository;
import com.linguanotes.repository.TagRepository;
import com.linguanotes.repository.UserAccountRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class NoteService {
    private final NoteRepository notes;
    private final LanguageRepository languages;
    private final TagRepository tags;
    private final UserAccountRepository accounts;
    private final LessonRepository lessons;
    private final NoteLikeRepository likes;
    private final NoteCommentRepository comments;

    public NoteService(NoteRepository notes, LanguageRepository languages, TagRepository tags,
                       UserAccountRepository accounts, LessonRepository lessons,
                       NoteLikeRepository likes, NoteCommentRepository comments) {
        this.notes = notes;
        this.languages = languages;
        this.tags = tags;
        this.accounts = accounts;
        this.lessons = lessons;
        this.likes = likes;
        this.comments = comments;
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> search(Long ownerId, String q, Long languageId, NoteType type,
                                     String unit, String tag, Boolean favorite) {
        Specification<Note> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.or(
                    builder.equal(root.get("owner").get("id"), ownerId),
                    builder.equal(root.get("visibility"), NoteVisibility.PUBLIC)));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("title")), like),
                        builder.like(builder.lower(root.get("content")), like)));
            }
            if (languageId != null) {
                predicates.add(builder.equal(root.get("language").get("id"), languageId));
            }
            if (type != null) {
                predicates.add(builder.equal(root.get("type"), type));
            }
            if (unit != null && !unit.isBlank()) {
                predicates.add(builder.like(builder.lower(root.get("unit")),
                        "%" + unit.trim().toLowerCase() + "%"));
            }
            if (Boolean.TRUE.equals(favorite)) {
                predicates.add(builder.isTrue(root.get("favorite")));
            }
            if (tag != null && !tag.isBlank()) {
                query.distinct(true);
                predicates.add(builder.equal(root.join("tags", JoinType.LEFT).get("name"), tag));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        return notes.findAll(specification, Sort.by(Sort.Direction.DESC, "updatedAt")).stream()
                .map(note -> map(note, ownerId))
                .toList();
    }

    @Transactional(readOnly = true)
    public NoteResponse get(Long id, Long ownerId) {
        return map(requireReadable(id, ownerId), ownerId);
    }

    public NoteResponse create(NoteRequest request, Long ownerId) {
        Note note = new Note();
        note.setOwner(accounts.getReferenceById(ownerId));
        apply(note, request, ownerId);
        return map(notes.saveAndFlush(note), ownerId);
    }

    public NoteResponse update(Long id, NoteRequest request, Long ownerId) {
        Note note = findOwned(id, ownerId);
        apply(note, request, ownerId);
        return map(notes.saveAndFlush(note), ownerId);
    }

    public NoteResponse autosave(Long id, NoteAutosaveRequest request, Long ownerId) {
        Note note = findOwned(id, ownerId);
        if (request.version() != null && request.version() != note.getVersion()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La note a été modifiée ailleurs. Rechargez-la avant de continuer.");
        }
        if (request.title() != null && !request.title().isBlank()) {
            note.setTitle(request.title().trim());
        }
        note.setContent(request.content());
        return map(notes.saveAndFlush(note), ownerId);
    }

    public NoteResponse toggleFavorite(Long id, Long ownerId) {
        Note note = findOwned(id, ownerId);
        note.setFavorite(!note.isFavorite());
        return map(notes.saveAndFlush(note), ownerId);
    }

    public void delete(Long id, Long ownerId) {
        notes.delete(findOwned(id, ownerId));
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> recentOwned(Long ownerId, int limit) {
        return notes.findTop5ByOwnerIdOrderByUpdatedAtDesc(ownerId).stream()
                .limit(limit)
                .map(note -> map(note, ownerId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Note> ownedNotes(Long ownerId) {
        return notes.findAllByOwnerId(ownerId);
    }

    @Transactional(readOnly = true)
    public Note requireReadable(Long id, Long ownerId) {
        Note note = notes.findById(id).orElseThrow(this::notFound);
        boolean readable = note.getOwner().getId().equals(ownerId)
                || note.getVisibility() == NoteVisibility.PUBLIC;
        if (!readable) {
            throw notFound();
        }
        return note;
    }

    private Note findOwned(Long id, Long ownerId) {
        return notes.findByIdAndOwnerId(id, ownerId).orElseThrow(this::notFound);
    }

    private void apply(Note note, NoteRequest request, Long ownerId) {
        note.setTitle(request.title().trim());
        note.setContent(request.content());
        note.setUnit(request.unit() == null || request.unit().isBlank() ? null : request.unit().trim());
        note.setType(request.type() == null ? NoteType.OTHER : request.type());
        note.setLevel(request.level() == null || request.level().isBlank() ? null : request.level().trim());
        note.setVisibility(request.visibility() == null ? NoteVisibility.PRIVATE : request.visibility());
        note.setFavorite(Boolean.TRUE.equals(request.favorite()));
        note.setLesson(request.lessonId() == null ? null : ownedLesson(ownerId, request.lessonId()));
        note.setLanguage(request.languageId() == null ? null : languages.findById(request.languageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Langue introuvable")));
        Set<Tag> selectedTags = new HashSet<>();
        if (request.tags() != null) {
            for (String raw : request.tags()) {
                String name = raw.trim();
                if (name.isEmpty()) continue;
                selectedTags.add(tags.findByNameIgnoreCase(name)
                        .orElseGet(() -> tags.save(new Tag(null, name))));
            }
        }
        note.setTags(selectedTags);
    }

    private Lesson ownedLesson(Long ownerId, Long lessonId) {
        Lesson lesson = lessons.findById(lessonId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leçon introuvable"));
        if (!lesson.getUnit().getCourse().getOwner().getId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leçon introuvable");
        }
        return lesson;
    }

    private NoteResponse map(Note note, Long viewerId) {
        Language language = note.getLanguage();
        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                language == null ? null : language.getId(),
                language == null ? null : language.getName(),
                note.getLesson() == null ? null : note.getLesson().getId(),
                note.getType(),
                note.getLevel(),
                note.getVisibility(),
                note.getOwner().getId(),
                note.getOwner().getName(),
                note.getUnit(),
                note.getTags().stream().map(Tag::getName).sorted().toList(),
                note.isFavorite(),
                note.getVersion(),
                note.getLastSavedAt(),
                note.getCreatedAt(),
                note.getUpdatedAt(),
                likes.countByNoteId(note.getId()),
                comments.countByNoteId(note.getId()),
                likes.existsByNoteIdAndUserId(note.getId(), viewerId));
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Note introuvable");
    }
}
