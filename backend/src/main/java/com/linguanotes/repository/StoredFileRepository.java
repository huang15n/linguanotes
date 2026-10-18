package com.linguanotes.repository;

import com.linguanotes.model.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {
    List<StoredFile> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    Optional<StoredFile> findByIdAndOwnerId(Long id, Long ownerId);
}