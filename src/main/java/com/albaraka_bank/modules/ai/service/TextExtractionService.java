package com.albaraka_bank.modules.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
public class TextExtractionService {

    public String extractText(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null) {
            return "Unknown content type";
        }

        try {
            if (contentType.equals("application/pdf")) {
                return extractFromPdf(file);
            } else if (contentType.startsWith("image/")) {
                return "Image content (OCR not available yet). Please verify visually.";
            } else {
                return "Unsupported file type for text extraction: " + contentType;
            }
        } catch (Exception e) {
            log.error("Failed to extract text from file: {}", file.getOriginalFilename(), e);
            return "Error extracting text: " + e.getMessage();
        }
    }

    private String extractFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return text.trim().isEmpty() ? "Empty PDF document" : text.trim();
        }
    }
}
