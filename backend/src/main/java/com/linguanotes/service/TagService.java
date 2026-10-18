package com.linguanotes.service;

import com.linguanotes.dto.TagRequest;
import com.linguanotes.dto.TagResponse;
import com.linguanotes.model.Tag;
import com.linguanotes.repository.TagRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class TagService {
    private final TagRepository tags;

    public TagService(TagRepository tags) {
        this.tags = tags;
    }

    @Transactional(readOnly = true)
    public List<TagResponse> list() {
        return tags.findAll(Sort.by("name")).stream().map(this::map).toList();
    }

    public TagResponse create(TagRequest request) {
        String name = normalize(request.name());
        if (tags.findByNameIgnoreCase(name).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette étiquette existe déjà");
        }
        return map(tags.save(new Tag(null, name)));
    }

    public TagResponse update(Long id, TagRequest request) {
        Tag tag = requireTag(id);
        String name = normalize(request.name());
        tags.findByNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette étiquette existe déjà");
                });
        tag.setName(name);
        return map(tags.save(tag));
    }

    public void delete(Long id) {
        tags.delete(requireTag(id));
    }

    @Transactional(readOnly = true)
    public Tag requireTag(Long id) {
        return tags.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Étiquette introuvable"));
    }

    private String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }

    private TagResponse map(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName());
    }
}
