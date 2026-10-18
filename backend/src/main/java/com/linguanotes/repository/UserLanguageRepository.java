package com.linguanotes.repository;

import com.linguanotes.model.UserLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserLanguageRepository extends JpaRepository<UserLanguage, Long> {
    List<UserLanguage> findAllByUserIdOrderByCreatedAtAsc(Long userId);
    Optional<UserLanguage> findByIdAndUserId(Long id, Long userId);
    Optional<UserLanguage> findByUserIdAndLanguageId(Long userId, Long languageId);
}
