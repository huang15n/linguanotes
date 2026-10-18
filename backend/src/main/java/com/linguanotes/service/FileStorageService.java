package com.linguanotes.service;

import com.linguanotes.dto.StoredFileResponse;
import com.linguanotes.model.StoredFile;
import com.linguanotes.repository.StoredFileRepository;
import com.linguanotes.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class FileStorageService {
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    private final StoredFileRepository files;
    private final UserAccountRepository accounts;
    private final Path storageRoot;

    public FileStorageService(StoredFileRepository files, UserAccountRepository accounts,
                              @Value("${app.file-storage-dir:./data/files}") String storageDirectory) {
        this.files = files;
        this.accounts = accounts;
        this.storageRoot = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    public StoredFile store(Long ownerId, MultipartFile upload) {
        if (upload == null || upload.isEmpty()) throw badRequest("Le fichier est vide");
        if (upload.getSize() > MAX_FILE_SIZE) throw badRequest("La taille maximale autorisée est de 20 Mo");
        String originalName = safeName(upload.getOriginalFilename());
        String extension = extension(originalName);
        String kind = classify(extension);
        byte[] content;
        try {
            content = upload.getBytes();
            verifySignature(extension, content);
            Files.createDirectories(storageRoot);
            String key = UUID.randomUUID() + "." + extension;
            Path target = storageRoot.resolve(key).normalize();
            if (!target.startsWith(storageRoot)) throw badRequest("Nom de fichier invalide");
            Files.write(target, content);

            StoredFile file = new StoredFile();
            file.setOwner(accounts.getReferenceById(ownerId));
            file.setStorageKey(key);
            file.setOriginalName(originalName);
            file.setContentType(contentType(extension));
            file.setFileSize(content.length);
            file.setSha256(sha256(content));
            file.setFileKind(kind);
            return files.save(file);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossible d’enregistrer le fichier");
        }
    }

    @Transactional(readOnly = true)
    public StoredFile findOwned(Long ownerId, Long fileId) {
        return files.findByIdAndOwnerId(fileId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable"));
    }

    @Transactional(readOnly = true)
    public java.util.List<StoredFileResponse> list(Long ownerId) {
        return files.findAllByOwnerIdOrderByCreatedAtDesc(ownerId).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public byte[] read(Long ownerId, Long fileId) {
        StoredFile file = findOwned(ownerId, fileId);
        return readFromStorage(file);
    }

    public void delete(Long ownerId, Long fileId) {
        StoredFile file = findOwned(ownerId, fileId);
        Path path = storageRoot.resolve(file.getStorageKey()).normalize();
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossible de supprimer le fichier");
        }
        files.delete(file);
    }

    public void deleteAllByOwner(Long ownerId) {
        java.util.List<StoredFile> ownedFiles = files.findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
        for (StoredFile file : ownedFiles) {
            Path path = storageRoot.resolve(file.getStorageKey()).normalize();
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Impossible de supprimer les fichiers du compte");
            }
        }
        files.deleteAll(ownedFiles);
    }

    public byte[] readFromStorage(StoredFile file) {
        Path path = storageRoot.resolve(file.getStorageKey()).normalize();
        if (!path.startsWith(storageRoot)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable");
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable");
        }
    }

    public StoredFileResponse response(StoredFile file) {
        return new StoredFileResponse(file.getId(), file.getOriginalName(), file.getContentType(),
                file.getFileSize(), file.getFileKind(), file.getCreatedAt());
    }

    private String classify(String extension) {
        return switch (extension) {
            case "pdf", "docx", "xlsx", "xls" -> "DOCUMENT";
            case "mp3", "wav", "ogg" -> "AUDIO";
            default -> throw badRequest("Format non pris en charge");
        };
    }

    private void verifySignature(String extension, byte[] bytes) {
        boolean valid = switch (extension) {
            case "pdf" -> startsWith(bytes, new byte[] {'%', 'P', 'D', 'F', '-'});
                case "docx", "xlsx" -> startsWith(bytes, new byte[] {'P', 'K', 3, 4});
                case "xls" -> startsWith(bytes, new byte[] {(byte) 0xd0, (byte) 0xcf, 0x11, (byte) 0xe0,
                    (byte) 0xa1, (byte) 0xb1, 0x1a, (byte) 0xe1});
            case "mp3" -> startsWith(bytes, new byte[] {'I', 'D', '3'}) ||
                    (bytes.length > 1 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xe0) == 0xe0);
            case "wav" -> bytes.length >= 12 && new String(bytes, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF") &&
                    new String(bytes, 8, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("WAVE");
            case "ogg" -> startsWith(bytes, new byte[] {'O', 'g', 'g', 'S'});
            default -> false;
        };
        if (!valid) throw badRequest("Le contenu du fichier ne correspond pas à son extension");
    }

    private boolean startsWith(byte[] input, byte[] signature) {
        if (input.length < signature.length) return false;
        for (int i = 0; i < signature.length; i++) if (input[i] != signature[i]) return false;
        return true;
    }

    private String contentType(String extension) {
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "xls" -> "application/vnd.ms-excel";
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "ogg" -> "audio/ogg";
            default -> "application/octet-stream";
        };
    }

    private String safeName(String value) {
        if (value == null || value.isBlank()) throw badRequest("Nom de fichier manquant");
        String base = value.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1).replaceAll("[^\\p{L}\\p{N}._ -]", "_").trim();
        if (base.isBlank() || base.length() > 255) throw badRequest("Nom de fichier invalide");
        return base;
    }

    private String extension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) throw badRequest("Extension de fichier manquante");
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}