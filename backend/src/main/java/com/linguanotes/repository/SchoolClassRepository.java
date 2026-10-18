package com.linguanotes.repository;

import com.linguanotes.model.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    List<SchoolClass> findAllByTeacherIdOrderByCreatedAtDesc(Long teacherId);
    Optional<SchoolClass> findByJoinCodeIgnoreCase(String joinCode);
}
