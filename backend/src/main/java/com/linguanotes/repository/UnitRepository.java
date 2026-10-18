package com.linguanotes.repository;

import com.linguanotes.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UnitRepository extends JpaRepository<Unit, Long> {
    List<Unit> findAllByCourseIdOrderByPositionAsc(Long courseId);
    Optional<Unit> findByIdAndCourseId(Long id, Long courseId);
}