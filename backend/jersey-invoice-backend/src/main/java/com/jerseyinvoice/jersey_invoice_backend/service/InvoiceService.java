package com.jerseyinvoice.jersey_invoice_backend.service;

import com.jerseyinvoice.jersey_invoice_backend.dto.InvoiceRequest;
import com.jerseyinvoice.jersey_invoice_backend.dto.InvoiceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
public class InvoiceService {
    private static final Object PROCESS_LOCK = new Object();
    private final PricingService pricingService;
    private final OpenPdfService pdfService;
    private final Path directory;

    public InvoiceService(PricingService pricingService, OpenPdfService pdfService,
                          @Value("${invoice.storage.directory:./generated-invoice}") String directory) {
        this.pricingService = pricingService;
        this.pdfService = pdfService;
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    public InvoiceResponse createInvoice(InvoiceRequest request) {
        InvoiceResponse.Pricing pricing = pricingService.calculate(request);
        synchronized (PROCESS_LOCK) {
            try {
                Files.createDirectories(directory);
                Path lockFile = directory.resolveSibling(directory.getFileName() + ".lock");
                try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                     var lock = channel.lock()) {
                    long count;
                    try (var files = Files.list(directory)) {
                        count = files.filter(Files::isRegularFile).count();
                    }
                    long next = 100000 + count;
                    if (next > 999999) throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoice number range is exhausted");
                    String number = "INV" + next;
                    Path destination = directory.resolve(number + ".pdf");
                    if (Files.exists(destination, LinkOption.NOFOLLOW_LINKS)) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Invoice number already exists; check the generated-invoice folder for missing or extra files");
                    }
                    Path temp = Files.createTempFile(directory.getParent(), ".invoice-", ".tmp");
                    try {
                        try (OutputStream output = Files.newOutputStream(temp)) {
                            pdfService.generate(output, number, request, pricing);
                        }
                        Files.move(temp, destination);
                    } finally {
                        Files.deleteIfExists(temp);
                    }
                    return new InvoiceResponse(number, "/api/invoices/" + number + "/pdf", pricing);
                }
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save the invoice PDF", e);
            }
        }
    }

    public Path findPdf(String number) {
        if (number == null || !number.matches("INV[0-9]{6}")) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Path path = directory.resolve(number + ".pdf");
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return path;
    }
}
