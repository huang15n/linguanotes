package com.linguanotes.service;

import com.linguanotes.dto.Dto.*;
import com.linguanotes.model.*;
import com.linguanotes.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@Transactional
public class CourseService {
    private final CourseRepository courses;
    private final UnitRepository units;
    private final LessonRepository lessons;
    private final LanguageRepository languages;
    private final UserAccountRepository accounts;

    public CourseService(CourseRepository courses, UnitRepository units, LessonRepository lessons,
                         LanguageRepository languages, UserAccountRepository accounts) {
        this.courses = courses;
        this.units = units;
        this.lessons = lessons;
        this.languages = languages;
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> listCourses(Long ownerId) {
        return courses.findAllByOwnerIdOrderByUpdatedAtDesc(ownerId).stream().map(this::map).toList();
    }

    public CourseResponse createCourse(Long ownerId, CourseRequest request) {
        Course course = new Course();
        course.setOwner(accounts.getReferenceById(ownerId));
        apply(course, request);
        return map(courses.save(course));
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourse(Long ownerId, Long courseId) {
        return map(requireCourse(ownerId, courseId));
    }

    public CourseResponse updateCourse(Long ownerId, Long courseId, CourseRequest request) {
        Course course = requireCourse(ownerId, courseId);
        apply(course, request);
        return map(courses.save(course));
    }

    public void deleteCourse(Long ownerId, Long courseId) {
        courses.delete(requireCourse(ownerId, courseId));
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> listUnits(Long ownerId, Long courseId) {
        requireCourse(ownerId, courseId);
        return units.findAllByCourseIdOrderByPositionAsc(courseId).stream().map(this::map).toList();
    }

    public UnitResponse createUnit(Long ownerId, Long courseId, UnitRequest request) {
        Unit unit = new Unit();
        unit.setCourse(requireCourse(ownerId, courseId));
        apply(unit, request);
        return map(units.save(unit));
    }

    public UnitResponse updateUnit(Long ownerId, Long courseId, Long unitId, UnitRequest request) {
        Unit unit = requireUnit(ownerId, courseId, unitId);
        apply(unit, request);
        return map(units.save(unit));
    }

    public void deleteUnit(Long ownerId, Long courseId, Long unitId) {
        units.delete(requireUnit(ownerId, courseId, unitId));
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> listLessons(Long ownerId, Long unitId) {
        requireOwnedUnit(ownerId, unitId);
        return lessons.findAllByUnitIdOrderByPositionAsc(unitId).stream().map(this::map).toList();
    }

    public LessonResponse createLesson(Long ownerId, Long unitId, LessonRequest request) {
        Lesson lesson = new Lesson();
        lesson.setUnit(requireOwnedUnit(ownerId, unitId));
        apply(lesson, request);
        return map(lessons.save(lesson));
    }

    public LessonResponse updateLesson(Long ownerId, Long unitId, Long lessonId, LessonRequest request) {
        Lesson lesson = lessons.findByIdAndUnitId(lessonId, unitId)
                .orElseThrow(() -> notFound("Leçon introuvable"));
        requireOwnedUnit(ownerId, unitId);
        apply(lesson, request);
        return map(lessons.save(lesson));
    }

    public void deleteLesson(Long ownerId, Long unitId, Long lessonId) {
        requireOwnedUnit(ownerId, unitId);
        lessons.delete(lessons.findByIdAndUnitId(lessonId, unitId)
                .orElseThrow(() -> notFound("Leçon introuvable")));
    }

    private Course requireCourse(Long ownerId, Long courseId) {
        return courses.findByIdAndOwnerId(courseId, ownerId)
                .orElseThrow(() -> notFound("Cours introuvable"));
    }

    private Unit requireUnit(Long ownerId, Long courseId, Long unitId) {
        requireCourse(ownerId, courseId);
        return units.findByIdAndCourseId(unitId, courseId)
                .orElseThrow(() -> notFound("Unité introuvable"));
    }

    private Unit requireOwnedUnit(Long ownerId, Long unitId) {
        Unit unit = units.findById(unitId).orElseThrow(() -> notFound("Unité introuvable"));
        if (!unit.getCourse().getOwner().getId().equals(ownerId)) {
            throw notFound("Unité introuvable");
        }
        return unit;
    }

    private void apply(Course course, CourseRequest request) {
        course.setTitle(request.title().trim());
        course.setLanguage(languages.findById(request.languageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Langue introuvable")));
        course.setLevel(request.level() == null || request.level().isBlank() ? null : request.level().trim());
        course.setDescription(request.description());
        course.setStatus(request.status() == null ? CourseStatus.DRAFT : request.status());
    }

    private void apply(Unit unit, UnitRequest request) {
        unit.setTitle(request.title().trim());
        unit.setPosition(Math.max(0, request.position()));
        unit.setDescription(request.description());
    }

    private void apply(Lesson lesson, LessonRequest request) {
        lesson.setTitle(request.title().trim());
        lesson.setContent(request.content());
        lesson.setType(request.type() == null ? LessonType.LESSON : request.type());
        lesson.setPosition(Math.max(0, request.position()));
    }

    private CourseResponse map(Course course) {
        return new CourseResponse(course.getId(), course.getTitle(), course.getLanguage().getId(),
                course.getLanguage().getName(), course.getLevel(), course.getDescription(), course.getStatus(),
                course.getCreatedAt(), course.getUpdatedAt());
    }

    private UnitResponse map(Unit unit) {
        return new UnitResponse(unit.getId(), unit.getCourse().getId(), unit.getTitle(),
                unit.getPosition(), unit.getDescription());
    }

    private LessonResponse map(Lesson lesson) {
        return new LessonResponse(lesson.getId(), lesson.getUnit().getId(), lesson.getTitle(),
                lesson.getContent(), lesson.getType(), lesson.getPosition());
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}