SELECT COUNT(*) INTO @notes_lesson_column_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'notes'
    AND COLUMN_NAME = 'lesson_id';
SET @notes_lesson_column_sql = IF(
        @notes_lesson_column_exists = 0,
        'ALTER TABLE notes ADD COLUMN lesson_id BIGINT NULL',
        'SELECT 1'
    );
PREPARE notes_lesson_column_stmt
FROM @notes_lesson_column_sql;
EXECUTE notes_lesson_column_stmt;
DEALLOCATE PREPARE notes_lesson_column_stmt;
SELECT COUNT(*) INTO @notes_lesson_fk_exists
FROM information_schema.REFERENTIAL_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'notes'
    AND CONSTRAINT_NAME = 'fk_notes_lesson';
SET @notes_lesson_fk_sql = IF(
        @notes_lesson_fk_exists = 0,
        'ALTER TABLE notes ADD CONSTRAINT fk_notes_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE SET NULL',
        'SELECT 1'
    );
PREPARE notes_lesson_fk_stmt
FROM @notes_lesson_fk_sql;
EXECUTE notes_lesson_fk_stmt;
DEALLOCATE PREPARE notes_lesson_fk_stmt;