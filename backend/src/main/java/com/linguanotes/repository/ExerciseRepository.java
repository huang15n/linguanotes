package com.linguanotes.repository;

import com.linguanotes.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    long countByOwnerId(Long ownerId);
    List<Exercise> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    Optional<Exercise> findByIdAndOwnerId(Long id, Long ownerId);
}