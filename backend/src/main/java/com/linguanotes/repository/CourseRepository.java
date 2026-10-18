package com.linguanotes.repository;

import com.linguanotes.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    long countByOwnerId(Long ownerId);
    List<Course> findAllByOwnerIdOrderByUpdatedAtDesc(Long ownerId);
    Optional<Course> findByIdAndOwnerId(Long id, Long ownerId);
}