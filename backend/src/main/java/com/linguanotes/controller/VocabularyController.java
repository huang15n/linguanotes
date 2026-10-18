package com.linguanotes.controller;

import com.linguanotes.dto.VocabularyImportReport;
import com.linguanotes.dto.VocabularyRequest;
import com.linguanotes.dto.VocabularyResponse;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.VocabularyExcelService;
import com.linguanotes.service.VocabularyService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/vocabulary")
public class VocabularyController {
    private final VocabularyService vocabulary;
    private final VocabularyExcelService excel;

    public VocabularyController(VocabularyService vocabulary, VocabularyExcelService excel) {
        this.vocabulary = vocabulary;
        this.excel = excel;
    }

    @GetMapping
    public List<VocabularyResponse> list(@AuthenticationPrincipal AccountPrincipal principal) {
        return vocabulary.list(principal.id());
    }

    @PostMapping
    public ResponseEntity<VocabularyResponse> create(@AuthenticationPrincipal AccountPrincipal principal,
                                                     @Valid @RequestBody VocabularyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vocabulary.create(principal.id(), request));
    }

    @PutMapping("/{id}")
    public VocabularyResponse update(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long id,
                                     @Valid @RequestBody VocabularyRequest request) {
        return vocabulary.update(principal.id(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long id) {
        vocabulary.delete(principal.id(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export.xlsx")
    public ResponseEntity<byte[]> export(@AuthenticationPrincipal AccountPrincipal principal) {
        byte[] content = excel.export(principal.id());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("linguanotes-vocabulaire.xlsx", StandardCharsets.UTF_8)
                        .build().toString())
                .body(content);
    }

    @PostMapping(value = "/import.xlsx", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public VocabularyImportReport importExcel(@AuthenticationPrincipal AccountPrincipal principal,
                                              @RequestPart("file") MultipartFile file) {
        return excel.importFile(principal.id(), file);
    }
}
