package com.linguanotes.service;

import com.linguanotes.dto.*;
import com.linguanotes.model.*;
import com.linguanotes.repository.LanguageRepository;
import com.linguanotes.repository.LessonRepository;
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
import java.util.*;
import java.util.stream.Collectors;

@Service @Transactional
public class NoteService {
    private final NoteRepository notes; private final LanguageRepository languages; private final TagRepository tags; private final UserAccountRepository accounts; private final LessonRepository lessons;
    public NoteService(NoteRepository n, LanguageRepository l, TagRepository t, UserAccountRepository a, LessonRepository lessonRepository) { notes = n; languages = l; tags = t; accounts = a; lessons = lessonRepository; }

    @Transactional(readOnly = true)
    public List<NoteResponse> search(Long ownerId, String q, Long languageId, NoteType type, String unit, String tag, Boolean favorite) {
        Specification<Note> spec = (root, cq, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(root.get("owner").get("id"), ownerId));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                p.add(cb.or(cb.like(cb.lower(root.get("title")), like), cb.like(cb.lower(root.get("content")), like)));
            }
            if (languageId != null) p.add(cb.equal(root.get("language").get("id"), languageId));
            if (type != null) p.add(cb.equal(root.get("type"), type));
            if (unit != null && !unit.isBlank()) p.add(cb.like(cb.lower(root.get("unit")), "%" + unit.trim().toLowerCase() + "%"));
            if (Boolean.TRUE.equals(favorite)) p.add(cb.isTrue(root.get("favorite")));
            if (tag != null && !tag.isBlank()) { cq.distinct(true); p.add(cb.equal(root.join("tags", JoinType.LEFT).get("name"), tag)); }
            return cb.and(p.toArray(new Predicate[0]));
        };
        return notes.findAll(spec, Sort.by(Sort.Direction.DESC, "updatedAt")).stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
        public NoteResponse get(Long id, Long ownerId) { return map(find(id, ownerId)); }

        public NoteResponse create(NoteRequest r, Long ownerId) { Note n = new Note(); n.setOwner(accounts.getReferenceById(ownerId)); apply(n, r, ownerId); return map(notes.save(n)); }
        public NoteResponse update(Long id, NoteRequest r, Long ownerId) { Note n = find(id, ownerId); apply(n, r, ownerId); return map(notes.save(n)); }
        public NoteResponse toggleFavorite(Long id, Long ownerId) { Note n = find(id, ownerId); n.setFavorite(!n.isFavorite()); return map(notes.save(n)); }
        public void delete(Long id, Long ownerId) { notes.delete(find(id, ownerId)); }

    @Transactional(readOnly = true)
        public DashboardResponse dashboard(Long ownerId) {
        Map<String, Long> byType = search(ownerId, null, null, null, null, null, null).stream()
            .collect(Collectors.groupingBy(n -> n.type().name(), Collectors.counting()));
        return new DashboardResponse(notes.countByOwnerId(ownerId), languages.count(),
            notes.countByOwnerIdAndFavoriteTrue(ownerId), byType,
            notes.findTop5ByOwnerIdOrderByUpdatedAtDesc(ownerId).stream().map(this::map).toList());
    }

    @Transactional(readOnly = true)
        public List<String> allTags(Long ownerId) { return tags.findNamesByNoteOwnerId(ownerId); }

        private Note find(Long id, Long ownerId) { return notes.findByIdAndOwnerId(id, ownerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note introuvable")); }

    private void apply(Note n, NoteRequest r, Long ownerId) {
        n.setTitle(r.title().trim());
        n.setContent(r.content());
        n.setUnit(r.unit() == null || r.unit().isBlank() ? null : r.unit().trim());
        n.setType(r.type() == null ? NoteType.OTHER : r.type());
        n.setFavorite(Boolean.TRUE.equals(r.favorite()));
        n.setLesson(r.lessonId() == null ? null : ownedLesson(ownerId, r.lessonId()));
        n.setLanguage(r.languageId() == null ? null
                : languages.findById(r.languageId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Langue introuvable")));
        Set<Tag> set = new HashSet<>();
        if (r.tags() != null) for (String raw : r.tags()) {
            String name = raw.trim();
            if (name.isEmpty()) continue;
            set.add(tags.findByNameIgnoreCase(name).orElseGet(() -> tags.save(new Tag(null, name))));
        }
        n.setTags(set);
    }

    private Lesson ownedLesson(Long ownerId, Long lessonId) {
        Lesson lesson = lessons.findById(lessonId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leçon introuvable"));
        if (!lesson.getUnit().getCourse().getOwner().getId().equals(ownerId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leçon introuvable");
        return lesson;
    }

    private NoteResponse map(Note n) {
        Language l = n.getLanguage();
        return new NoteResponse(n.getId(), n.getTitle(), n.getContent(), l == null ? null : l.getId(), l == null ? null : l.getName(),
            n.getLesson() == null ? null : n.getLesson().getId(),
                n.getType(), n.getUnit(), n.getTags().stream().map(Tag::getName).sorted().toList(), n.isFavorite(), n.getCreatedAt(), n.getUpdatedAt());
    }
}
