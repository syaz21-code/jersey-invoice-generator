package com.jerseyinvoice.jersey_invoice_backend;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InvoiceControllerIntegrationTests {
    private static final Path DIRECTORY = createTestDirectory();
    @LocalServerPort private int port;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("invoice.storage.directory", () -> DIRECTORY.toString());
    }

    private static Path createTestDirectory() {
        try {
            Path parent = Path.of("target").toAbsolutePath();
            Files.createDirectories(parent);
            return Files.createTempDirectory(parent, "invoice-test-");
        } catch (Exception e) { throw new ExceptionInInitializerError(e); }
    }

    @Test
    void calculatesAndStoresPdfAndDoesNotOverwriteOnCollision() throws Exception {
        String json = """
                {
                  "date": "2026-11-24",
                  "customer": { "name": "Ali", "phone": "0123456789", "address": "Kuala Lumpur" },
                  "jobs": [
                    { "jobType": "STANDARD", "jobName": "Team", "design": "Custom Design (+RM50)",
                      "sizes": { "m": 3, "xl7": 2 }, "material": "Diamond 155GSM (+RM2)",
                      "collar": "Roundneck", "sleeve": "Sleeveless (-RM2)", "nameset": "None",
                      "cutting": "Standard", "piping": "None",
                      "specialRequests": [{ "name": "Logo", "price": 5.00, "chargeType": "PCS" }] },
                    { "jobType": "PAINTBALL", "jobName": "Paintball", "design": "Customer Provided", "sizes": { "l": 1 } }
                  ],
                  "specialRequests": [{ "name": "Rush", "price": 20.00, "chargeType": "ONE" }]
                }
                """;
        HttpResponse<String> first = post(json);
        assertEquals(201, first.statusCode(), first.body());
        assertTrue(first.body().contains("\"number\":\"INV100000\""), first.body());
        assertTrue(first.body().contains("\"total\":393"), first.body());
        Path firstPdf = DIRECTORY.resolve("INV100000.pdf");
        assertTrue(Files.isRegularFile(firstPdf));
        PdfReader reader = new PdfReader(Files.readAllBytes(firstPdf));
        StringBuilder text = new StringBuilder();
        for (int page = 1; page <= reader.getNumberOfPages(); page++) text.append(new PdfTextExtractor(reader).getTextFromPage(page));
        reader.close();
        assertTrue(text.toString().contains("INV100000"), text.toString());
        assertTrue(text.toString().contains("393.00"), text.toString());
        assertTrue(text.toString().contains("Paintball"), text.toString());
        HttpResponse<byte[]> pdf = HttpClient.newHttpClient().send(HttpRequest.newBuilder(uri("/api/invoices/INV100000/pdf")).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200, pdf.statusCode());
        assertArrayEquals(Files.readAllBytes(firstPdf), pdf.body());

        HttpResponse<String> invalid = post(json.replace("\"phone\": \"0123456789\"", "\"phone\": \"\""));
        assertEquals(400, invalid.statusCode());
        assertEquals(201, post(json).statusCode());
        Path secondPdf = DIRECTORY.resolve("INV100001.pdf");
        byte[] secondBytes = Files.readAllBytes(secondPdf);
        Files.delete(firstPdf);
        HttpResponse<String> collision = post(json);
        assertEquals(409, collision.statusCode());
        assertTrue(collision.body().contains("Invoice number already exists"));
        assertArrayEquals(secondBytes, Files.readAllBytes(secondPdf));

        Files.write(firstPdf, pdf.body());
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest concurrentRequest = HttpRequest.newBuilder(uri("/api/invoices"))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
        var left = client.sendAsync(concurrentRequest, HttpResponse.BodyHandlers.ofString());
        var right = client.sendAsync(concurrentRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, left.get().statusCode());
        assertEquals(201, right.get().statusCode());
        assertTrue(Files.isRegularFile(DIRECTORY.resolve("INV100002.pdf")));
        assertTrue(Files.isRegularFile(DIRECTORY.resolve("INV100003.pdf")));
    }

    private URI uri(String path) { return URI.create("http://localhost:" + port + path); }
    private HttpResponse<String> post(String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri("/api/invoices")).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }
}
