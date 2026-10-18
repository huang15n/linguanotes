package com.linguanotes.controller;

import com.linguanotes.dto.*;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CourseController {
    private final CourseService courses;

    public CourseController(CourseService courses) {
        this.courses = courses;
    }

    @GetMapping("/courses")
    public List<CourseResponse> list(@AuthenticationPrincipal AccountPrincipal principal) {
        return courses.listCourses(principal.id());
    }

    @PostMapping("/courses")
    public ResponseEntity<CourseResponse> create(@AuthenticationPrincipal AccountPrincipal principal,
                                                 @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courses.createCourse(principal.id(), request));
    }

    @GetMapping("/courses/{courseId}")
    public CourseResponse get(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long courseId) {
        return courses.getCourse(principal.id(), courseId);
    }

    @PutMapping("/courses/{courseId}")
    public CourseResponse update(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long courseId,
                                 @Valid @RequestBody CourseRequest request) {
        return courses.updateCourse(principal.id(), courseId, request);
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long courseId) {
        courses.deleteCourse(principal.id(), courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses/{courseId}/units")
    public List<UnitResponse> listUnits(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long courseId) {
        return courses.listUnits(principal.id(), courseId);
    }

    @PostMapping("/courses/{courseId}/units")
    public ResponseEntity<UnitResponse> createUnit(@AuthenticationPrincipal AccountPrincipal principal,
                                                   @PathVariable Long courseId, @Valid @RequestBody UnitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courses.createUnit(principal.id(), courseId, request));
    }

    @PutMapping("/courses/{courseId}/units/{unitId}")
    public UnitResponse updateUnit(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long courseId,
                                   @PathVariable Long unitId, @Valid @RequestBody UnitRequest request) {
        return courses.updateUnit(principal.id(), courseId, unitId, request);
    }

    @DeleteMapping("/courses/{courseId}/units/{unitId}")
    public ResponseEntity<Void> deleteUnit(@AuthenticationPrincipal AccountPrincipal principal,
                                           @PathVariable Long courseId, @PathVariable Long unitId) {
        courses.deleteUnit(principal.id(), courseId, unitId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/units/{unitId}/lessons")
    public List<LessonResponse> listLessons(@AuthenticationPrincipal AccountPrincipal principal,
                                            @PathVariable Long unitId) {
        return courses.listLessons(principal.id(), unitId);
    }

    @PostMapping("/units/{unitId}/lessons")
    public ResponseEntity<LessonResponse> createLesson(@AuthenticationPrincipal AccountPrincipal principal,
                                                       @PathVariable Long unitId, @Valid @RequestBody LessonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courses.createLesson(principal.id(), unitId, request));
    }

    @PutMapping("/units/{unitId}/lessons/{lessonId}")
    public LessonResponse updateLesson(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long unitId,
                                       @PathVariable Long lessonId, @Valid @RequestBody LessonRequest request) {
        return courses.updateLesson(principal.id(), unitId, lessonId, request);
    }

    @DeleteMapping("/units/{unitId}/lessons/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@AuthenticationPrincipal AccountPrincipal principal,
                                             @PathVariable Long unitId, @PathVariable Long lessonId) {
        courses.deleteLesson(principal.id(), unitId, lessonId);
        return ResponseEntity.noContent().build();
    }
}