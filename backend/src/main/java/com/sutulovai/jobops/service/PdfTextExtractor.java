package com.sutulovai.jobops.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class PdfTextExtractor {

    private static final Logger log = LoggerFactory.getLogger(PdfTextExtractor.class);
    private static final int MAX_TEXT_LENGTH = 50_000;

    public String extract(InputStream inputStream) {
        try (var doc = Loader.loadPDF(inputStream.readAllBytes())) {
            var stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            var text = stripper.getText(doc);
            log.info("✅ Extracted {} chars from PDF", text.length());
            if (text.length() > MAX_TEXT_LENGTH) {
                return text.substring(0, MAX_TEXT_LENGTH) + "\n[...truncated]";
            }
            return text;
        } catch (IOException e) {
            log.warn("⚠️ PDF extraction failed: {}", e.getMessage());
            return "";
        }
    }
}
