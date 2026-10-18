package com.linguanotes.repository;

import com.linguanotes.model.LanguageLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LanguageLevelRepository extends JpaRepository<LanguageLevel, Long> {
    List<LanguageLevel> findAllByLanguageIdOrderByPositionAsc(Long languageId);
}
