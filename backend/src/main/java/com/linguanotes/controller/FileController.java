package com.linguanotes.controller;

import com.linguanotes.dto.StoredFileResponse;
import com.linguanotes.model.StoredFile;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.FileStorageService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileStorageService storage;

    public FileController(FileStorageService storage) {
        this.storage = storage;
    }

    @GetMapping
    public List<StoredFileResponse> list(@AuthenticationPrincipal AccountPrincipal principal) {
        return storage.list(principal.id());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoredFileResponse> upload(@AuthenticationPrincipal AccountPrincipal principal,
                                                      @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(201).body(storage.response(storage.store(principal.id(), file)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> download(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long id) {
        StoredFile file = storage.findOwned(principal.id(), id);
        byte[] bytes = storage.readFromStorage(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.getOriginalName(), StandardCharsets.UTF_8).build().toString())
                .body(bytes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long id) {
        storage.delete(principal.id(), id);
        return ResponseEntity.noContent().build();
    }
}