package com.linguanotes.repository;

import com.linguanotes.model.LessonSection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LessonSectionRepository extends JpaRepository<LessonSection, Long> {
    List<LessonSection> findAllByLessonIdOrderByPositionAsc(Long lessonId);
    Optional<LessonSection> findByIdAndLessonId(Long id, Long lessonId);
}
