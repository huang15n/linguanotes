-- Partage d'une note avec une classe pédagogique.
ALTER TABLE notes
    ADD COLUMN class_id BIGINT NULL;

ALTER TABLE notes
    ADD CONSTRAINT fk_notes_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE SET NULL;

CREATE INDEX idx_notes_class ON notes(class_id);
