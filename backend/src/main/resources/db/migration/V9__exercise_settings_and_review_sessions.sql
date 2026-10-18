-- Paramètres des exercices, mode aperçu et sessions de révision.
ALTER TABLE exercises
    ADD COLUMN language_id BIGINT NULL,
    ADD COLUMN level VARCHAR(30) NULL,
    ADD COLUMN grammar_topic VARCHAR(180) NULL,
    ADD COLUMN introduction LONGTEXT NULL,
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    ADD COLUMN max_attempts INT NULL,
    ADD COLUMN pass_threshold INT NOT NULL DEFAULT 60,
    ADD COLUMN show_answers BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN updated_at DATETIME(6) NULL;

ALTER TABLE exercises
    ADD CONSTRAINT fk_exercises_language FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE SET NULL;

UPDATE exercises SET updated_at = created_at WHERE updated_at IS NULL;

ALTER TABLE exercise_attempts
    ADD COLUMN preview BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE review_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    language_id BIGINT NULL,
    mode VARCHAR(24) NOT NULL,
    total_words INT NOT NULL DEFAULT 0,
    correct_count INT NOT NULL DEFAULT 0,
    incorrect_count INT NOT NULL DEFAULT 0,
    started_at DATETIME(6) NOT NULL,
    completed_at DATETIME(6) NULL,
    CONSTRAINT fk_review_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_sessions_language FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE SET NULL,
    INDEX idx_review_sessions_user_date (user_id, started_at)
);

CREATE TABLE review_session_items (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    vocabulary_entry_id BIGINT NULL,
    correct BOOLEAN NOT NULL,
    answered_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_review_items_session FOREIGN KEY (session_id) REFERENCES review_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_items_vocabulary FOREIGN KEY (vocabulary_entry_id) REFERENCES vocabulary_entries(id) ON DELETE SET NULL,
    INDEX idx_review_items_session (session_id)
);
