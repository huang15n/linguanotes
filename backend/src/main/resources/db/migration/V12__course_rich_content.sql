-- Un cours fonctionne comme une note : contenu riche en Markdown.
ALTER TABLE courses
    ADD COLUMN content LONGTEXT NULL;
