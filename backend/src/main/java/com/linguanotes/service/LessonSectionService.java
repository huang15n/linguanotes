package com.linguanotes.service;

import com.linguanotes.dto.LessonSectionRequest;
import com.linguanotes.dto.LessonSectionResponse;
import com.linguanotes.model.Lesson;
import com.linguanotes.model.LessonSection;
import com.linguanotes.repository.LessonRepository;
import com.linguanotes.repository.LessonSectionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class LessonSectionService {
    private final LessonSectionRepository sections;
    private final LessonRepository lessons;

    public LessonSectionService(LessonSectionRepository sections, LessonRepository lessons) {
        this.sections = sections;
        this.lessons = lessons;
    }

    @Transactional(readOnly = true)
    public List<LessonSectionResponse> list(Long ownerId, Long lessonId) {
        requireOwnedLesson(ownerId, lessonId);
        return sections.findAllByLessonIdOrderByPositionAsc(lessonId).stream().map(this::map).toList();
    }

    public LessonSectionResponse create(Long ownerId, Long lessonId, LessonSectionRequest request) {
        Lesson lesson = requireOwnedLesson(ownerId, lessonId);
        LessonSection section = new LessonSection();
        section.setLesson(lesson);
        apply(section, request);
        return map(sections.saveAndFlush(section));
    }

    public LessonSectionResponse update(Long ownerId, Long lessonId, Long sectionId,
                                        LessonSectionRequest request) {
        requireOwnedLesson(ownerId, lessonId);
        LessonSection section = sections.findByIdAndLessonId(sectionId, lessonId)
                .orElseThrow(() -> notFound());
        apply(section, request);
        return map(sections.saveAndFlush(section));
    }

    public void delete(Long ownerId, Long lessonId, Long sectionId) {
        requireOwnedLesson(ownerId, lessonId);
        sections.delete(sections.findByIdAndLessonId(sectionId, lessonId)
                .orElseThrow(this::notFound));
    }

    private Lesson requireOwnedLesson(Long ownerId, Long lessonId) {
        Lesson lesson = lessons.findById(lessonId).orElseThrow(this::notFound);
        if (!lesson.getUnit().getCourse().getOwner().getId().equals(ownerId)) {
            throw notFound();
        }
        return lesson;
    }

    private void apply(LessonSection section, LessonSectionRequest request) {
        section.setType(request.type());
        section.setTitle(request.title().trim());
        section.setContent(request.content());
        section.setContentJson(request.contentJson());
        section.setPosition(Math.max(0, request.position()));
        section.setVisible(request.visible() == null || request.visible());
    }

    private LessonSectionResponse map(LessonSection section) {
        return new LessonSectionResponse(section.getId(), section.getLesson().getId(),
                section.getType(), section.getTitle(), section.getContent(),
                section.getContentJson(), section.getPosition(), section.isVisible(),
                section.getCreatedAt(), section.getUpdatedAt());
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Section de leçon introuvable");
    }
}
