-- Profils utilisateurs complets, langues étudiées et classes pédagogiques.
ALTER TABLE users
    ADD COLUMN first_name VARCHAR(80) NULL,
    ADD COLUMN last_name VARCHAR(80) NULL,
    ADD COLUMN display_name VARCHAR(120) NULL,
    ADD COLUMN avatar_url VARCHAR(500) NULL,
    ADD COLUMN country VARCHAR(80) NULL,
    ADD COLUMN timezone VARCHAR(60) NULL,
    ADD COLUMN native_language_id BIGINT NULL,
    ADD COLUMN last_login_at DATETIME(6) NULL,
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE users
    ADD CONSTRAINT fk_users_native_language FOREIGN KEY (native_language_id) REFERENCES languages(id) ON DELETE SET NULL;

CREATE TABLE user_languages (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    language_id BIGINT NOT NULL,
    level VARCHAR(30) NULL,
    target BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_user_languages UNIQUE (user_id, language_id),
    CONSTRAINT fk_user_languages_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_languages_language FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE CASCADE,
    INDEX idx_user_languages_user (user_id)
);

CREATE TABLE classes (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT NOT NULL,
    name VARCHAR(180) NOT NULL,
    description TEXT NULL,
    join_code VARCHAR(12) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_classes_join_code UNIQUE (join_code),
    CONSTRAINT fk_classes_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_classes_teacher (teacher_id)
);

CREATE TABLE class_members (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    member_role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    joined_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_class_members UNIQUE (class_id, user_id),
    CONSTRAINT fk_class_members_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_class_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_class_members_user (user_id)
);

CREATE TABLE class_exercises (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    assigned_at DATETIME(6) NOT NULL,
    due_at DATETIME(6) NULL,
    CONSTRAINT uk_class_exercises UNIQUE (class_id, exercise_id),
    CONSTRAINT fk_class_exercises_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_class_exercises_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
);
