package com.albaraka_bank;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;

public class PdfGenerator {

    public static void main(String[] args) {
        try {
            File dir = new File("test-docs");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 1. Valid Invoice (Should Approve)
            createPdf("test-docs/valid_invoice.pdf",
                    "INVOICE #INV-2024-001",
                    "Date: 2024-01-03",
                    "To: Al Baraka Bank Client",
                    "Service: IT Consulting Services",
                    "Amount: 15000.00 DH",
                    "Status: Unpaid");

            // 2. Invalid Amount (Should Reject)
            createPdf("test-docs/fraud_invoice.pdf",
                    "INVOICE #INV-2024-002",
                    "Date: 2024-01-03",
                    "To: Al Baraka Bank Client",
                    "Service: Small Repair",
                    "Amount: 500.00 DH",
                    "Status: Unpaid");

            // 3. Irrelevant Document (Should be Human Review or Reject)
            createPdf("test-docs/recipe.pdf",
                    "Grandma's Chocolate Cake",
                    "Ingredients:",
                    "- 2 cups flour",
                    "- 1 cup sugar",
                    "- 3 eggs",
                    "Instructions: Mix well and bake at 350F.");

            // 4. Verification Document (Proof of AI)
            createPdf("test-docs/verification_test.pdf",
                    "AL BARAKA BANK - AI SYSTEM VERIFICATION",
                    "Date: 2024-01-03",
                    "User: Malik",
                    "Secret Code: ALPHA-BRAVO-CHARLIE",
                    "Purpose: To prove the AI reads this text.",
                    "Amount: 0.00 DH");

            System.out.println("Test PDFs generated in 'test-docs/' folder.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createPdf(String filename, String... lines) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.newLineAtOffset(50, 700);
                content.setLeading(14.5f);

                for (String line : lines) {
                    content.showText(line);
                    content.newLine();
                }
                content.endText();
            }
            doc.save(filename);
            System.out.println("Generated: " + filename);
        }
    }
}
