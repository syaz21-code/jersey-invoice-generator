package com.jerseyinvoice.jersey_invoice_backend.service;

import com.jerseyinvoice.jersey_invoice_backend.dto.InvoiceRequest;
import com.jerseyinvoice.jersey_invoice_backend.dto.InvoiceResponse;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;

/** Renders the same invoice contents and six-column table as the original browser PDF. */
@Service
public class OpenPdfService {
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
    private static final Font TABLE_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

    public void generate(OutputStream output, String number, InvoiceRequest request, InvoiceResponse.Pricing pricing) {
        Document doc = new Document(PageSize.A4, 40, 40, 35, 35);
        try {
            PdfWriter.getInstance(doc, output);
            doc.open();
            doc.add(new Paragraph("JERSEY PRINTING SERVICE", BOLD));
            doc.add(new Paragraph("Mohamad Syazreeq Akmal\nakmalsyazreeq@gmail.com\n+60199483874", NORMAL));
            Paragraph title = new Paragraph("INVOICE", TITLE);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(28);
            title.setSpacingAfter(18);
            doc.add(title);
            doc.add(new Paragraph("Date:  " + request.date(), NORMAL));
            doc.add(new Paragraph("No. Invoice:  " + number, NORMAL));
            Paragraph bill = new Paragraph("Bill to:", BOLD);
            bill.setSpacingBefore(25);
            doc.add(bill);
            InvoiceRequest.CustomerRequest customer = request.customer();
            doc.add(new Paragraph(text(customer.companyName()) + "\n" + customer.address() + "\n\nU.P.: "
                    + customer.name() + "\n" + text(customer.email()) + "\n" + customer.phone(), NORMAL));

            PdfPTable table = new PdfPTable(new float[] { 1.2f, 3.6f, 1.2f, 1.1f, 0.7f, 1.2f });
            table.setWidthPercentage(100);
            table.setSpacingBefore(26);
            for (String heading : List.of("Date", "Description", "Size", "Rate", "Qty", "Total")) {
                PdfPCell header = new PdfPCell(new Phrase(heading, TABLE_HEADER));
                header.setPadding(4);
                header.setBorder(Rectangle.NO_BORDER);
                header.setBackgroundColor(new Color(41, 128, 185));
                table.addCell(header);
            }
            table.setHeaderRows(1);
            String date = request.date().toString();
            for (int i = 0; i < request.jobs().size(); i++) {
                InvoiceRequest.JobRequest job = request.jobs().get(i);
                InvoiceResponse.JobPricing p = pricing.jobs().get(i);
                String description = job.jobType() == InvoiceRequest.JobType.PAINTBALL
                        ? "JOB: " + job.jobName() + "\nPACKAGE: Paintball Jersey Package\nDESIGN - " + job.design()
                        : "JOB: " + job.jobName() + "\nMaterial: " + job.material() + "\nCollar: " + job.collar()
                        + "\nSleeve: " + job.sleeve() + "\nDesign: " + job.design();
                row(table, date, description, sizeText(job.sizes()), p.rate(), p.quantity(), p.baseTotal());
                charge(table, date, "SLEEVE", job.jobName(), p.sleeve(), p.quantity());
                charge(table, date, "COLLAR", job.jobName(), p.collar(), p.quantity());
                charge(table, date, "MATERIAL", job.jobName(), p.material(), p.quantity());
                charge(table, date, "CUTTING", job.jobName(), p.cutting(), p.quantity());
                charge(table, date, "NAMESET", job.jobName(), p.nameset(), p.quantity());
                charge(table, date, "PIPING", job.jobName(), p.piping(), p.quantity());
                if (p.design().signum() != 0) row(table, date, "DESIGN (" + job.jobName() + ") - " + job.design(), "-", p.design(), 1, p.design());
                long plus = (long) job.sizes().xl4() + job.sizes().xl5();
                long big = (long) job.sizes().xl6() + job.sizes().xl7();
                if (p.plusSize().signum() != 0) row(table, date, "PLUS SIZE 4XL-5XL (" + job.jobName() + ")", Long.toString(plus), BigDecimal.valueOf(5), plus, p.plusSize());
                if (p.bigSize().signum() != 0) row(table, date, "PLUS SIZE 6XL-7XL (" + job.jobName() + ")", Long.toString(big), BigDecimal.valueOf(8), big, p.bigSize());
                for (InvoiceRequest.SpecialRequest special : requests(job.specialRequests())) special(table, date, "JOB: " + job.jobName() + " - ", special, p.quantity());
            }
            for (InvoiceRequest.SpecialRequest special : requests(request.specialRequests())) special(table, date, "GLOBAL: ", special, pricing.quantity());
            if (pricing.postage().signum() != 0) row(table, date, "POSTAGE", "-", pricing.postage(), 1, pricing.postage());
            doc.add(table);
            Paragraph total = new Paragraph("Total: RM" + amount(pricing.total()), BOLD);
            total.setAlignment(Element.ALIGN_RIGHT);
            total.setSpacingBefore(15);
            doc.add(total);
            Paragraph notes = new Paragraph("This is a computer-generated invoice and does not require a signature or company stamp.\n\n"
                    + "Payment Method: BANK TRANSFER\nBank Name: Maybank\nAccount Number: 162272989602\n\nNOTES\n"
                    + "1. Please put INVOICE NUMBER on the reference\n   section upon payment.\n"
                    + "2. All defects, problems and etc should be informed to\n   us within 48 hours upon received. Any days from the\n   time given will not be accepted.", NORMAL);
            notes.setSpacingBefore(25);
            notes.setKeepTogether(true);
            doc.add(notes);
        } catch (DocumentException e) {
            throw new IllegalStateException("Could not generate invoice PDF", e);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    private static void charge(PdfPTable table, String date, String name, String job, BigDecimal total, long qty) {
        if (total.signum() != 0) row(table, date, name + " (" + job + ")", "-", total.divide(BigDecimal.valueOf(qty)), qty, total);
    }
    private static void special(PdfPTable table, String date, String prefix, InvoiceRequest.SpecialRequest item, long quantity) {
        long qty = item.chargeType() == InvoiceRequest.ChargeType.PCS ? quantity : 1;
        row(table, date, prefix + item.name(), "-", item.price(), qty, item.price().multiply(BigDecimal.valueOf(qty)));
    }
    private static List<InvoiceRequest.SpecialRequest> requests(List<InvoiceRequest.SpecialRequest> value) { return value == null ? List.of() : value; }
    private static String text(String value) { return value == null ? "" : value; }
    private static String amount(BigDecimal value) { return value.setScale(2).toPlainString(); }
    private static void row(PdfPTable table, String date, String description, String size, BigDecimal rate, long qty, BigDecimal total) {
        boolean shaded = table.size() % 2 == 1;
        for (String value : List.of(date, description, size, amount(rate), Long.toString(qty), amount(total))) {
            PdfPCell cell = new PdfPCell(new Phrase(value, NORMAL));
            cell.setPadding(4);
            cell.setBorder(Rectangle.NO_BORDER);
            if (shaded) cell.setBackgroundColor(new Color(245, 245, 245));
            table.addCell(cell);
        }
    }
    private static String sizeText(InvoiceRequest.Sizes sizes) {
        String[] labels = { "XS", "S", "M", "L", "XL", "2XL", "3XL", "4XL", "5XL", "6XL", "7XL" };
        int[] quantities = { sizes.xs(), sizes.s(), sizes.m(), sizes.l(), sizes.xl(), sizes.xl2(), sizes.xl3(), sizes.xl4(), sizes.xl5(), sizes.xl6(), sizes.xl7() };
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < labels.length; i++) if (quantities[i] > 0) {
            if (!result.isEmpty()) result.append('\n');
            result.append(labels[i]).append('-').append(quantities[i]);
        }
        return result.toString();
    }
}
