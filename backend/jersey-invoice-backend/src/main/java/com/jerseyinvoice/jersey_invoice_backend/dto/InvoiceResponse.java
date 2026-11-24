package com.jerseyinvoice.jersey_invoice_backend.dto;

import java.math.BigDecimal;
import java.util.List;

/** The generated file and the server-calculated amounts. */
public record InvoiceResponse(String number, String pdfUrl, Pricing pricing) {
    public record Pricing(long quantity, List<JobPricing> jobs, BigDecimal jobsTotal,
                          BigDecimal specialRequestsTotal, BigDecimal postage, BigDecimal total) {}
    public record JobPricing(long quantity, BigDecimal basePrice, BigDecimal rate, BigDecimal baseTotal,
                             BigDecimal material, BigDecimal collar, BigDecimal sleeve, BigDecimal cutting,
                             BigDecimal nameset, BigDecimal piping, BigDecimal design, BigDecimal plusSize,
                             BigDecimal bigSize, BigDecimal specialRequests, BigDecimal total) {}
}
