CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(16) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);
CREATE TABLE password_reset_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT uk_password_reset_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_password_reset_user (user_id)
);
ALTER TABLE notes
ADD COLUMN owner_id BIGINT NULL;
ALTER TABLE notes
ADD CONSTRAINT fk_notes_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE;
CREATE INDEX idx_notes_owner_updated ON notes(owner_id, updated_at);
CREATE TABLE courses (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    language_id BIGINT NOT NULL,
    title VARCHAR(180) NOT NULL,
    level VARCHAR(40) NULL,
    description TEXT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_courses_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_courses_language FOREIGN KEY (language_id) REFERENCES languages(id),
    INDEX idx_courses_owner_status (owner_id, status)
);
CREATE TABLE units (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(180) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    description TEXT NULL,
    CONSTRAINT fk_units_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    INDEX idx_units_course_position (course_id, sort_order)
);
CREATE TABLE lessons (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    unit_id BIGINT NOT NULL,
    title VARCHAR(180) NOT NULL,
    content LONGTEXT NULL,
    lesson_type VARCHAR(24) NOT NULL DEFAULT 'LESSON',
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_lessons_unit FOREIGN KEY (unit_id) REFERENCES units(id) ON DELETE CASCADE,
    INDEX idx_lessons_unit_position (unit_id, sort_order)
);
CREATE TABLE vocabulary_entries (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    lesson_id BIGINT NULL,
    source_language_id BIGINT NOT NULL,
    target_language_id BIGINT NOT NULL,
    source_text VARCHAR(500) NOT NULL,
    translation VARCHAR(1000) NOT NULL,
    ipa VARCHAR(255) NULL,
    part_of_speech VARCHAR(80) NULL,
    examples_json JSON NULL,
    personal_notes TEXT NULL,
    difficulty TINYINT NOT NULL DEFAULT 1,
    audio_file_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_vocabulary_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_vocabulary_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE
    SET NULL,
        CONSTRAINT fk_vocabulary_source_language FOREIGN KEY (source_language_id) REFERENCES languages(id),
        CONSTRAINT fk_vocabulary_target_language FOREIGN KEY (target_language_id) REFERENCES languages(id),
        INDEX idx_vocabulary_owner_source (owner_id, source_text)
);
CREATE TABLE exercises (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    lesson_id BIGINT NULL,
    title VARCHAR(180) NOT NULL,
    instructions TEXT NOT NULL,
    exercise_type VARCHAR(32) NOT NULL,
    questions_json JSON NOT NULL,
    explanations_json JSON NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_exercises_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_exercises_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE
    SET NULL,
        INDEX idx_exercises_owner (owner_id)
);
CREATE TABLE exercise_attempts (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    exercise_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    score DECIMAL(7, 2) NOT NULL,
    answers_json JSON NOT NULL,
    attempted_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_attempts_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE,
    CONSTRAINT fk_attempts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_attempts_user_date (user_id, attempted_at)
);
CREATE TABLE flashcards (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    lesson_id BIGINT NULL,
    vocabulary_entry_id BIGINT NULL,
    front_text TEXT NOT NULL,
    back_text TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_flashcards_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_flashcards_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE
    SET NULL,
        CONSTRAINT fk_flashcards_vocabulary FOREIGN KEY (vocabulary_entry_id) REFERENCES vocabulary_entries(id) ON DELETE
    SET NULL,
        INDEX idx_flashcards_owner (owner_id)
);
CREATE TABLE review_progress (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    flashcard_id BIGINT NOT NULL,
    repetitions INT NOT NULL DEFAULT 0,
    correct_count INT NOT NULL DEFAULT 0,
    incorrect_count INT NOT NULL DEFAULT 0,
    interval_days INT NOT NULL DEFAULT 0,
    ease_factor DECIMAL(4, 2) NOT NULL DEFAULT 2.50,
    last_reviewed_at DATETIME(6) NULL,
    next_due_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_review_user_card UNIQUE (user_id, flashcard_id),
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_flashcard FOREIGN KEY (flashcard_id) REFERENCES flashcards(id) ON DELETE CASCADE,
    INDEX idx_review_due (user_id, next_due_at)
);
CREATE TABLE stored_files (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(160) NOT NULL,
    file_size BIGINT NOT NULL,
    sha256 VARCHAR(64) NOT NULL,
    file_kind VARCHAR(24) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_stored_file_key UNIQUE (storage_key),
    CONSTRAINT fk_stored_files_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_stored_files_owner (owner_id)
);
ALTER TABLE vocabulary_entries
ADD CONSTRAINT fk_vocabulary_audio FOREIGN KEY (audio_file_id) REFERENCES stored_files(id) ON DELETE
SET NULL;
CREATE TABLE import_jobs (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    extracted_content LONGTEXT NULL,
    error_message TEXT NULL,
    course_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    confirmed_at DATETIME(6) NULL,
    CONSTRAINT fk_imports_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_imports_file FOREIGN KEY (file_id) REFERENCES stored_files(id) ON DELETE CASCADE,
    CONSTRAINT fk_imports_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE
    SET NULL,
        INDEX idx_imports_owner_created (owner_id, created_at)
);
CREATE TABLE ai_generations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    generation_type VARCHAR(32) NOT NULL,
    status VARCHAR(20) NOT NULL,
    prompt_hash VARCHAR(64) NOT NULL,
    result_content LONGTEXT NULL,
    input_tokens INT NULL,
    output_tokens INT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_ai_generations_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_ai_owner_created (owner_id, created_at)
);
CREATE TABLE user_preferences (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    interface_locale VARCHAR(10) NOT NULL DEFAULT 'fr',
    daily_goal_minutes INT NOT NULL DEFAULT 15,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_user_preferences_user UNIQUE (user_id),
    CONSTRAINT fk_user_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);