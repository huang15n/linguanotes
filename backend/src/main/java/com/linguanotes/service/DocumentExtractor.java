package com.linguanotes.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class DocumentExtractor {
    private static final int MAX_EXTRACTED_CHARS = 1_000_000;

    public String extract(String fileName, byte[] bytes) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(java.util.Locale.ROOT);
        try {
            String text = switch (extension) {
                case "docx" -> word(bytes);
                case "xlsx", "xls" -> spreadsheet(bytes);
                case "pdf" -> pdf(bytes);
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format de document non pris en charge");
            };
            text = text.strip();
            if (text.isBlank()) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Aucun texte exploitable n’a été trouvé; les PDF scannés nécessitent un module OCR.");
            if (text.length() > MAX_EXTRACTED_CHARS) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "Le document extrait dépasse la taille maximale autorisée.");
            return text;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le document est illisible ou endommagé");
        }
    }

    private String word(byte[] bytes) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            StringBuilder text = new StringBuilder();
            document.getParagraphs().forEach(paragraph -> text.append(paragraph.getText()).append('\n'));
            document.getTables().forEach(table -> table.getRows().forEach(row -> {
                row.getTableCells().forEach(cell -> text.append(cell.getText()).append('\t'));
                text.append('\n');
            }));
            return text.toString();
        }
    }

    private String spreadsheet(byte[] bytes) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            DataFormatter formatter = new DataFormatter();
            StringBuilder text = new StringBuilder();
            workbook.forEach(sheet -> {
                text.append("# ").append(sheet.getSheetName()).append('\n');
                for (Row row : sheet) {
                    boolean hasValue = false;
                    for (int i = row.getFirstCellNum(); i >= 0 && i < row.getLastCellNum(); i++) {
                        String value = formatter.formatCellValue(row.getCell(i));
                        if (!value.isBlank()) hasValue = true;
                        text.append(value).append('\t');
                    }
                    if (hasValue) text.append('\n');
                }
            });
            return text.toString();
        }
    }

    private String pdf(byte[] bytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(document);
        }
    }
}