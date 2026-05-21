package com.shopease.payment.service;

import com.shopease.payment.dto.InvoiceDto;
import org.openpdf.text.*;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfService {

    public ByteArrayInputStream generateInvoice(InvoiceDto data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            String title = "Invoice";
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Font.BOLD);
            Paragraph paragraph = new Paragraph(title, font);
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            document.add(new Paragraph(""));
            document.add(new Paragraph("Invoice no: " + data.invoiceNumber()));
            document.add(new Paragraph("CustomerID: " + data.customerId()));
            document.add(new Paragraph("Email: " + data.email()));
            document.add(new Paragraph("Amount: " + data.amount()));
            document.add(new Paragraph("Payment Date: " + data.paymentDate()));
            document.close();
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Invoice");
        }
    }
}
