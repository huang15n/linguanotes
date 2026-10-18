package com.linguanotes.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linguanotes.dto.ConjugationTable;
import com.linguanotes.dto.VocabularyImportReport;
import com.linguanotes.dto.VocabularyRequest;
import com.linguanotes.dto.VocabularyResponse;
import com.linguanotes.model.LearningStatus;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VocabularyExcelService {
    private static final String[] HEADERS = {
            "id", "sourceLanguageId", "targetLanguageId", "sourceText", "translation",
            "partOfSpeech", "grammaticalGender", "level", "definition", "ipa",
            "examples", "synonyms", "antonyms", "personalNotes", "difficulty",
            "learningStatus", "region", "pluralForm", "conjugations", "tags"
    };
    private static final TypeReference<List<ConjugationTable>> CONJUGATIONS = new TypeReference<>() { };

    private final VocabularyService vocabulary;
    private final ObjectMapper json;

    public VocabularyExcelService(VocabularyService vocabulary, ObjectMapper json) {
        this.vocabulary = vocabulary;
        this.json = json;
    }

    public byte[] export(Long ownerId) {
        List<VocabularyResponse> entries = vocabulary.list(ownerId);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Vocabulaire");
            CellStyle headerStyle = headerStyle(workbook);
            Row header = sheet.createRow(0);
            for (int index = 0; index < HEADERS.length; index++) {
                Cell cell = header.createCell(index);
                cell.setCellValue(HEADERS[index]);
                cell.setCellStyle(headerStyle);
            }
            int rowIndex = 1;
            for (VocabularyResponse entry : entries) {
                Row row = sheet.createRow(rowIndex++);
                int column = 0;
                row.createCell(column++).setCellValue(entry.id());
                row.createCell(column++).setCellValue(entry.sourceLanguageId());
                row.createCell(column++).setCellValue(entry.targetLanguageId());
                row.createCell(column++).setCellValue(entry.sourceText());
                row.createCell(column++).setCellValue(entry.translation());
                row.createCell(column++).setCellValue(value(entry.partOfSpeech()));
                row.createCell(column++).setCellValue(value(entry.grammaticalGender()));
                row.createCell(column++).setCellValue(value(entry.level()));
                row.createCell(column++).setCellValue(value(entry.definition()));
                row.createCell(column++).setCellValue(value(entry.ipa()));
                row.createCell(column++).setCellValue(String.join(" | ", entry.examples()));
                row.createCell(column++).setCellValue(String.join(" | ", entry.synonyms()));
                row.createCell(column++).setCellValue(String.join(" | ", entry.antonyms()));
                row.createCell(column++).setCellValue(value(entry.personalNotes()));
                row.createCell(column++).setCellValue(entry.difficulty());
                row.createCell(column++).setCellValue(entry.learningStatus().name());
                row.createCell(column++).setCellValue(value(entry.region()));
                row.createCell(column++).setCellValue(value(entry.pluralForm()));
                row.createCell(column++).setCellValue(write(entry.conjugations()));
                row.createCell(column).setCellValue(String.join(" | ", entry.tags()));
            }
            for (int index = 0; index < HEADERS.length; index++) {
                sheet.autoSizeColumn(index);
                if (sheet.getColumnWidth(index) > 12_000) sheet.setColumnWidth(index, 12_000);
            }
            sheet.createFreezePane(0, 1);

            Sheet conjugationSheet = workbook.createSheet("Conjugaisons");
            Row conjugationHeader = conjugationSheet.createRow(0);
            String[] headers = {"vocabularyId", "sourceText", "tableTitle", "mood", "tense",
                    "variant", "tableNote", "person", "form", "rowNote"};
            for (int index = 0; index < headers.length; index++) {
                Cell cell = conjugationHeader.createCell(index);
                cell.setCellValue(headers[index]);
                cell.setCellStyle(headerStyle);
            }
            int conjugationRow = 1;
            for (VocabularyResponse entry : entries) {
                for (ConjugationTable table : entry.conjugations()) {
                    for (var form : table.rows()) {
                        Row row = conjugationSheet.createRow(conjugationRow++);
                        row.createCell(0).setCellValue(entry.id());
                        row.createCell(1).setCellValue(entry.sourceText());
                        row.createCell(2).setCellValue(value(table.title()));
                        row.createCell(3).setCellValue(value(table.mood()));
                        row.createCell(4).setCellValue(value(table.tense()));
                        row.createCell(5).setCellValue(value(table.variant()));
                        row.createCell(6).setCellValue(value(table.note()));
                        row.createCell(7).setCellValue(value(form.person()));
                        row.createCell(8).setCellValue(value(form.form()));
                        row.createCell(9).setCellValue(value(form.note()));
                    }
                }
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Impossible de générer le fichier Excel", exception);
        }
    }

    public VocabularyImportReport importFile(Long ownerId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le fichier est vide");
        }
        List<String> errors = new ArrayList<>();
        int imported = 0;
        int failed = 0;
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheet("Vocabulaire");
            if (sheet == null) sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(0);
            if (header == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La première ligne doit contenir les en-têtes");
            }
            Map<String, Integer> columns = new HashMap<>();
            for (Cell cell : header) {
                columns.put(formatter.formatCellValue(cell).trim(), cell.getColumnIndex());
            }
            if (!columns.containsKey("sourceLanguageId") || !columns.containsKey("targetLanguageId")
                    || !columns.containsKey("sourceText") || !columns.containsKey("translation")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Colonnes obligatoires manquantes : sourceLanguageId, targetLanguageId, sourceText, translation");
            }
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;
                try {
                    String sourceId = cell(row, columns, "sourceLanguageId", formatter);
                    String targetId = cell(row, columns, "targetLanguageId", formatter);
                    String sourceText = cell(row, columns, "sourceText", formatter);
                    String translation = cell(row, columns, "translation", formatter);
                    if (sourceId.isBlank() && targetId.isBlank() && sourceText.isBlank() && translation.isBlank()) {
                        continue;
                    }
                    VocabularyRequest request = new VocabularyRequest(
                            null,
                            Long.parseLong(sourceId),
                            Long.parseLong(targetId),
                            sourceText,
                            translation,
                            nullable(cell(row, columns, "ipa", formatter)),
                            nullable(cell(row, columns, "partOfSpeech", formatter)),
                            nullable(cell(row, columns, "grammaticalGender", formatter)),
                            nullable(cell(row, columns, "level", formatter)),
                            nullable(cell(row, columns, "definition", formatter)),
                            split(cell(row, columns, "examples", formatter)),
                            split(cell(row, columns, "synonyms", formatter)),
                            split(cell(row, columns, "antonyms", formatter)),
                            nullable(cell(row, columns, "personalNotes", formatter)),
                            integer(cell(row, columns, "difficulty", formatter)),
                            status(cell(row, columns, "learningStatus", formatter)),
                            nullable(cell(row, columns, "region", formatter)),
                            nullable(cell(row, columns, "pluralForm", formatter)),
                            conjugations(cell(row, columns, "conjugations", formatter)),
                            split(cell(row, columns, "tags", formatter)));
                    vocabulary.create(ownerId, request);
                    imported++;
                } catch (RuntimeException exception) {
                    failed++;
                    errors.add("Ligne " + (rowIndex + 1) + " : " + exception.getMessage());
                }
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le fichier Excel est illisible", exception);
        }
        return new VocabularyImportReport(imported, failed, errors);
    }

    private String cell(Row row, Map<String, Integer> columns, String name, DataFormatter formatter) {
        Integer index = columns.get(name);
        if (index == null) return "";
        Cell cell = row.getCell(index);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    private String nullable(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private List<String> split(String value) {
        if (value == null || value.isBlank()) return List.of();
        return java.util.Arrays.stream(value.split("\\s*\\|\\s*"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }

    private Integer integer(String value) {
        try {
            return value == null || value.isBlank() ? 1 : Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    private LearningStatus status(String value) {
        try {
            return value == null || value.isBlank() ? LearningStatus.NEW : LearningStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return LearningStatus.NEW;
        }
    }

    private List<ConjugationTable> conjugations(String value) {
        if (value == null || value.isBlank()) return List.of();
        try {
            return json.readValue(value, CONJUGATIONS);
        } catch (JsonProcessingException exception) {
            return List.of();
        }
    }

    private String write(List<ConjugationTable> conjugations) {
        try {
            return json.writeValueAsString(conjugations == null ? List.of() : conjugations);
        } catch (JsonProcessingException exception) {
            return "[]";
        }
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
