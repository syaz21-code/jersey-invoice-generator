package com.jerseyinvoice.jersey_invoice_backend;

import com.jerseyinvoice.jersey_invoice_backend.dto.InvoiceRequest;
import com.jerseyinvoice.jersey_invoice_backend.service.PricingService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTests {
    @Test
    void choosesTheCorrectTierAtEachQuantityBoundary() {
        int[][] cases = {{1, 37}, {10, 37}, {11, 32}, {20, 32}, {21, 26},
                {60, 26}, {61, 24}, {100, 24}, {101, 22}};
        PricingService pricing = new PricingService();
        for (int[] example : cases) {
            var sizes = new InvoiceRequest.Sizes(0, 0, example[0], 0, 0, 0, 0, 0, 0, 0, 0);
            var job = new InvoiceRequest.JobRequest(InvoiceRequest.JobType.STANDARD, "Team", "Customer Provided",
                    sizes, "Eyelet 150GSM", "Roundneck", "Short Sleeve", "None", "Standard", "None", null);
            var request = new InvoiceRequest(LocalDate.of(2026, 11, 24), null,
                    new InvoiceRequest.CustomerRequest("Ali", null, null, "123", "Address"), List.of(job), null);
            var result = pricing.calculate(request);
            assertEquals(0, BigDecimal.valueOf(example[1]).compareTo(result.jobs().getFirst().basePrice()));
            assertEquals(0, BigDecimal.valueOf((long) example[0] * example[1]).compareTo(result.total()));
        }
    }
}
