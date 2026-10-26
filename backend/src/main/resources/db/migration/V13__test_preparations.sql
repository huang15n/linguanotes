-- Préparation aux examens officiels (séparé des notes).
CREATE TABLE IF NOT EXISTS test_preparations (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id        BIGINT       NOT NULL,
    title           VARCHAR(255) NOT NULL,
    content         TEXT,
    exam_language   VARCHAR(20)  NOT NULL,
    exam_type       VARCHAR(20)  NOT NULL,
    exam_level      VARCHAR(30)  NOT NULL,
    skill_module    VARCHAR(20)  NOT NULL,
    visibility      VARCHAR(20)  NOT NULL DEFAULT 'PRIVATE',
    version         INT          NOT NULL DEFAULT 0,
    favorite        BOOLEAN      NOT NULL DEFAULT FALSE,
    audio_file_id   BIGINT       NULL,
    last_saved_at   DATETIME(6)  NULL,
    created_at      DATETIME(6)  NULL,
    updated_at      DATETIME(6)  NULL,
    CONSTRAINT fk_test_prep_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_test_prep_audio FOREIGN KEY (audio_file_id) REFERENCES stored_files (id) ON DELETE SET NULL
);

CREATE INDEX idx_test_prep_owner ON test_preparations (owner_id);
CREATE INDEX idx_test_prep_exam ON test_preparations (exam_language, exam_type, skill_module);
