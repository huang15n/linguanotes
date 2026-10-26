-- Statut de publication des préparations d'examen (brouillon / publié).
ALTER TABLE test_preparations
    ADD COLUMN publication_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' AFTER skill_module;

CREATE INDEX idx_test_prep_status ON test_preparations (publication_status);
