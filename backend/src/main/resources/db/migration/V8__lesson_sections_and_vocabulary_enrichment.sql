-- Sections pédagogiques flexibles et enrichissement des fiches de vocabulaire.
CREATE TABLE lesson_sections (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    lesson_id BIGINT NOT NULL,
    section_type VARCHAR(32) NOT NULL,
    title VARCHAR(180) NOT NULL,
    content LONGTEXT NULL,
    content_json JSON NULL,
    sort_order INT NOT NULL DEFAULT 0,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_lesson_sections_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    INDEX idx_lesson_sections_lesson_order (lesson_id, sort_order)
);

ALTER TABLE vocabulary_entries
    ADD COLUMN level VARCHAR(30) NULL,
    ADD COLUMN grammatical_gender VARCHAR(20) NULL,
    ADD COLUMN definition TEXT NULL,
    ADD COLUMN synonyms_json JSON NULL,
    ADD COLUMN antonyms_json JSON NULL,
    ADD COLUMN learning_status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    ADD COLUMN region VARCHAR(80) NULL,
    ADD COLUMN plural_form VARCHAR(255) NULL,
    ADD COLUMN conjugation_json JSON NULL,
    ADD COLUMN updated_at DATETIME(6) NULL;

UPDATE vocabulary_entries SET updated_at = created_at WHERE updated_at IS NULL;
