package com.linguanotes.repository;

import com.linguanotes.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LanguageRepository extends JpaRepository<Language, Long> {
	Optional<Language> findByCodeIgnoreCase(String code);
}
