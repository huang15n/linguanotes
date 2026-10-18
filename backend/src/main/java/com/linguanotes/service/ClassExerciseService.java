package com.linguanotes.service;

import com.linguanotes.dto.ClassExerciseRequest;
import com.linguanotes.dto.ClassExerciseResponse;
import com.linguanotes.model.ClassExercise;
import com.linguanotes.model.Exercise;
import com.linguanotes.model.SchoolClass;
import com.linguanotes.repository.ClassExerciseRepository;
import com.linguanotes.repository.ClassMemberRepository;
import com.linguanotes.repository.ExerciseRepository;
import com.linguanotes.repository.SchoolClassRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ClassExerciseService {
    private final ClassExerciseRepository assignments;
    private final SchoolClassRepository classes;
    private final ClassMemberRepository members;
    private final ExerciseRepository exercises;

    public ClassExerciseService(ClassExerciseRepository assignments, SchoolClassRepository classes,
                                ClassMemberRepository members, ExerciseRepository exercises) {
        this.assignments = assignments;
        this.classes = classes;
        this.members = members;
        this.exercises = exercises;
    }

    @Transactional(readOnly = true)
    public List<ClassExerciseResponse> list(Long classId, Long userId) {
        requireAccess(classId, userId);
        return assignments.findAllBySchoolClassIdOrderByAssignedAtDesc(classId).stream()
                .map(this::map)
                .toList();
    }

    public ClassExerciseResponse assign(Long classId, Long userId, ClassExerciseRequest request) {
        SchoolClass schoolClass = requireOwner(classId, userId);
        Exercise exercise = exercises.findByIdAndOwnerId(request.exerciseId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Exercice introuvable ou non autorisé"));
        ClassExercise assignment = assignments
                .findBySchoolClassIdAndExerciseId(classId, exercise.getId())
                .orElseGet(() -> {
                    ClassExercise created = new ClassExercise();
                    created.setSchoolClass(schoolClass);
                    created.setExercise(exercise);
                    return created;
                });
        assignment.setDueAt(request.dueAt());
        return map(assignments.saveAndFlush(assignment));
    }

    public void remove(Long classId, Long assignmentId, Long userId) {
        requireOwner(classId, userId);
        assignments.delete(assignments.findByIdAndSchoolClassId(assignmentId, classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Affectation introuvable")));
    }

    private SchoolClass requireOwner(Long classId, Long userId) {
        SchoolClass schoolClass = classes.findById(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classe introuvable"));
        if (!schoolClass.getTeacher().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
        }
        return schoolClass;
    }

    private SchoolClass requireAccess(Long classId, Long userId) {
        SchoolClass schoolClass = classes.findById(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classe introuvable"));
        boolean allowed = schoolClass.getTeacher().getId().equals(userId)
                || members.findBySchoolClassIdAndUserId(classId, userId).isPresent();
        if (!allowed) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
        return schoolClass;
    }

    private ClassExerciseResponse map(ClassExercise assignment) {
        return new ClassExerciseResponse(assignment.getId(), assignment.getSchoolClass().getId(),
                assignment.getExercise().getId(), assignment.getExercise().getTitle(),
                assignment.getExercise().getOwner().getId(), assignment.getAssignedAt(),
                assignment.getDueAt());
    }
}
