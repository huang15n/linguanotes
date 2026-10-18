package com.linguanotes.repository;

import com.linguanotes.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findAllByUnitIdOrderByPositionAsc(Long unitId);
    Optional<Lesson> findByIdAndUnitId(Long id, Long unitId);
}