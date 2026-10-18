package com.linguanotes.repository;

import com.linguanotes.model.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {
    List<ImportJob> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    Optional<ImportJob> findByIdAndOwnerId(Long id, Long ownerId);
}