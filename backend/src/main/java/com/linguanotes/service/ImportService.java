package com.linguanotes.service;

import com.linguanotes.dto.*;
import com.linguanotes.model.*;
import com.linguanotes.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ImportService {
    private final FileStorageService storage;
    private final DocumentExtractor extractor;
    private final ImportJobRepository jobs;
    private final StoredFileRepository files;
    private final UserAccountRepository accounts;
    private final CourseRepository courseRepository;
    private final CourseService courses;

    public ImportService(FileStorageService storage, DocumentExtractor extractor, ImportJobRepository jobs,
                         StoredFileRepository files, UserAccountRepository accounts,
                         CourseRepository courseRepository, CourseService courses) {
        this.storage = storage;
        this.extractor = extractor;
        this.jobs = jobs;
        this.files = files;
        this.accounts = accounts;
        this.courseRepository = courseRepository;
        this.courses = courses;
    }

    public ImportJobResponse upload(Long ownerId, MultipartFile upload) {
        StoredFile file = storage.store(ownerId, upload);
        if (!"DOCUMENT".equals(file.getFileKind())) {
            storage.delete(ownerId, file.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seuls les documents PDF, DOCX, XLSX et XLS peuvent être importés");
        }

        ImportJob job = new ImportJob();
        job.setOwner(accounts.getReferenceById(ownerId));
        job.setFile(file);
        try {
            job.setExtractedContent(extractor.extract(file.getOriginalName(), storage.readFromStorage(file)));
            job.setStatus(ImportStatus.PREVIEW);
        } catch (ResponseStatusException e) {
            job.setStatus(ImportStatus.FAILED);
            job.setErrorMessage(e.getReason());
        }
        return response(jobs.save(job));
    }

    @Transactional(readOnly = true)
    public List<ImportJobResponse> list(Long ownerId) {
        return jobs.findAllByOwnerIdOrderByCreatedAtDesc(ownerId).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public ImportJobResponse get(Long ownerId, Long id) {
        return response(find(ownerId, id));
    }

    public ImportJobResponse confirm(Long ownerId, Long id, ConfirmImportRequest request) {
        ImportJob job = find(ownerId, id);
        if (job.getStatus() != ImportStatus.PREVIEW) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seuls les imports en aperçu peuvent être confirmés");
        }
        CourseResponse course;
        if (request.courseId() != null) {
            course = courses.getCourse(ownerId, request.courseId());
        } else {
            if (request.languageId() == null || request.courseTitle() == null || request.courseTitle().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choisissez un cours ou renseignez son titre et sa langue");
            }
            course = courses.createCourse(ownerId, new CourseRequest(request.courseTitle(), request.languageId(), null,
                    "Cours créé pendant l’importation", CourseStatus.DRAFT));
        }
        UnitResponse unit = courses.createUnit(ownerId, course.id(), new UnitRequest(request.unitTitle(), 0,
                "Document importé : " + job.getFile().getOriginalName()));
        courses.createLesson(ownerId, unit.id(), new LessonRequest(job.getFile().getOriginalName(),
                job.getExtractedContent(), LessonType.LESSON, 0));
        job.setCourse(courseRepository.getReferenceById(course.id()));
        job.setStatus(ImportStatus.CONFIRMED);
        job.setConfirmedAt(LocalDateTime.now());
        return response(jobs.save(job));
    }

    private ImportJob find(Long ownerId, Long id) {
        return jobs.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Import introuvable"));
    }

    private ImportJobResponse response(ImportJob job) {
        String content = job.getExtractedContent();
        String preview = content == null ? null : content.substring(0, Math.min(content.length(), 12_000));
        return new ImportJobResponse(job.getId(), job.getFile().getOriginalName(), job.getStatus().name(),
                preview, job.getErrorMessage(), job.getCourse() == null ? null : job.getCourse().getId(), job.getCreatedAt());
    }
}