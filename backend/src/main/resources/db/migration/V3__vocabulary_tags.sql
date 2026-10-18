CREATE TABLE vocabulary_entry_tags (
    vocabulary_entry_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (vocabulary_entry_id, tag_id),
    CONSTRAINT fk_vocabulary_entry_tags_entry FOREIGN KEY (vocabulary_entry_id) REFERENCES vocabulary_entries(id) ON DELETE CASCADE,
    CONSTRAINT fk_vocabulary_entry_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id)
);