package com.linguanotes.config;

import com.linguanotes.model.Language;
import com.linguanotes.repository.LanguageRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean CommandLineRunner seed(LanguageRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(new Language(null, "Español", "es"));
                repo.save(new Language(null, "Français", "fr"));
                repo.save(new Language(null, "English", "en"));
            }
        };
    }
}
