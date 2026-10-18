-- Référentiel des niveaux par langue et métadonnées d'apprentissage des notes.
CREATE TABLE language_levels (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    language_id BIGINT NOT NULL,
    code VARCHAR(30) NOT NULL,
    label VARCHAR(80) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_language_levels_language FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE CASCADE,
    CONSTRAINT uk_language_levels_code UNIQUE (language_id, code),
    INDEX idx_language_levels_order (language_id, sort_order)
);

ALTER TABLE notes
    ADD COLUMN level VARCHAR(30) NULL AFTER type,
    ADD COLUMN visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE' AFTER level,
    ADD COLUMN version INT NOT NULL DEFAULT 0,
    ADD COLUMN last_saved_at DATETIME(6) NULL;

-- Anglais : IELTS.
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'IELTS_5', 'IELTS 5', 5 FROM languages WHERE code = 'en';
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'IELTS_6', 'IELTS 6', 6 FROM languages WHERE code = 'en';
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'IELTS_7', 'IELTS 7', 7 FROM languages WHERE code = 'en';
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'IELTS_8', 'IELTS 8', 8 FROM languages WHERE code = 'en';
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'IELTS_9', 'IELTS 9', 9 FROM languages WHERE code = 'en';

-- Français : CECRL.
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'A1', 'A1', 1 FROM languages WHERE code = 'fr';
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'A2', 'A2', 2 FROM languages WHERE code = 'fr';
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'B1', 'B1', 3 FROM languages WHERE code = 'fr';
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'B2', 'B2', 4 FROM languages WHERE code = 'fr';
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'C1', 'C1', 5 FROM languages WHERE code = 'fr';
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'C2', 'C2', 6 FROM languages WHERE code = 'fr';

-- Espagnol : CECRL.
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'A1', 'A1', 1 FROM languages WHERE code = 'es';
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'A2', 'A2', 2 FROM languages WHERE code = 'es';
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'B1', 'B1', 3 FROM languages WHERE code = 'es';
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'B2', 'B2', 4 FROM languages WHERE code = 'es';
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'C1', 'C1', 5 FROM languages WHERE code = 'es';
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'C2', 'C2', 6 FROM languages WHERE code = 'es';

-- Chinois : caractères traditionnels et simplifiés.
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'TRADITIONAL', 'Traditionnel', 1 FROM languages WHERE code = 'zh-CN';
INSERT IGNORE INTO language_levels (language_id, code, label, sort_order)
SELECT id, 'SIMPLIFIED', 'Simplifié', 2 FROM languages WHERE code = 'zh-CN';
