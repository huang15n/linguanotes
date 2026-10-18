package com.linguanotes.repository;

import com.linguanotes.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageRepository extends JpaRepository<Language, Long> { }
