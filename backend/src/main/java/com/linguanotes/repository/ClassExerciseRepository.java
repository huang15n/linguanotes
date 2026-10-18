package com.linguanotes.repository;

import com.linguanotes.model.ClassExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClassExerciseRepository extends JpaRepository<ClassExercise, Long> {
    List<ClassExercise> findAllBySchoolClassIdOrderByAssignedAtDesc(Long classId);
    Optional<ClassExercise> findByIdAndSchoolClassId(Long id, Long classId);
    Optional<ClassExercise> findBySchoolClassIdAndExerciseId(Long classId, Long exerciseId);
}
