package com.linguanotes.controller;

import com.linguanotes.dto.ConfirmImportRequest;
import com.linguanotes.dto.ImportJobResponse;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.ImportService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/imports")
public class ImportController {
    private final ImportService imports;

    public ImportController(ImportService imports) {
        this.imports = imports;
    }

    @GetMapping
    public List<ImportJobResponse> list(@AuthenticationPrincipal AccountPrincipal principal) {
        return imports.list(principal.id());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportJobResponse> upload(@AuthenticationPrincipal AccountPrincipal principal,
                                                     @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(201).body(imports.upload(principal.id(), file));
    }

    @GetMapping("/{id}")
    public ImportJobResponse get(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long id) {
        return imports.get(principal.id(), id);
    }

    @PostMapping("/{id}/confirm")
    public ImportJobResponse confirm(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long id,
                                     @Valid @RequestBody ConfirmImportRequest request) {
        return imports.confirm(principal.id(), id, request);
    }
}