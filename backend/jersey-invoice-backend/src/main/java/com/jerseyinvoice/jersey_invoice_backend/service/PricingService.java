package com.jerseyinvoice.jersey_invoice_backend.service;

import com.jerseyinvoice.jersey_invoice_backend.dto.InvoiceRequest;
import com.jerseyinvoice.jersey_invoice_backend.dto.InvoiceResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PricingService {
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    public InvoiceResponse.Pricing calculate(InvoiceRequest invoice) {
        List<InvoiceResponse.JobPricing> jobs = invoice.jobs().stream().map(this::calculateJob).toList();
        long quantity = jobs.stream().mapToLong(InvoiceResponse.JobPricing::quantity).sum();
        BigDecimal jobsTotal = jobs.stream().map(InvoiceResponse.JobPricing::total).reduce(ZERO, BigDecimal::add);
        BigDecimal special = requestsTotal(invoice.specialRequests(), quantity);
        BigDecimal postage = invoice.postagePrice() == null ? ZERO : invoice.postagePrice();
        return new InvoiceResponse.Pricing(quantity, jobs, jobsTotal, special, postage,
                jobsTotal.add(special).add(postage));
    }

    private InvoiceResponse.JobPricing calculateJob(InvoiceRequest.JobRequest job) {
        InvoiceRequest.Sizes s = job.sizes();
        long quantity = (long) s.xs() + s.s() + s.m() + s.l() + s.xl() + s.xl2() + s.xl3() + s.xl4() + s.xl5() + s.xl6() + s.xl7();
        BigDecimal base = BigDecimal.valueOf(quantity <= 10 ? 37 : quantity <= 20 ? 32 : quantity <= 60 ? 26 : quantity <= 100 ? 24 : 22);
        boolean paintball = job.jobType() == InvoiceRequest.JobType.PAINTBALL;
        BigDecimal rate = paintball ? base.add(BigDecimal.valueOf(60)) : base;
        BigDecimal baseTotal = rate.multiply(BigDecimal.valueOf(quantity));
        BigDecimal material = paintball ? ZERO : perPiece("material", job.material(), quantity);
        BigDecimal collar = paintball ? ZERO : perPiece("collar", job.collar(), quantity);
        BigDecimal sleeve = paintball ? ZERO : perPiece("sleeve", job.sleeve(), quantity);
        BigDecimal cutting = paintball ? ZERO : perPiece("cutting", job.cutting(), quantity);
        BigDecimal nameset = paintball ? ZERO : perPiece("nameset", job.nameset(), quantity);
        BigDecimal piping = paintball ? ZERO : perPiece("piping", job.piping(), quantity);
        BigDecimal design = PricingCatalog.option("design", job.design());
        BigDecimal plus = paintball ? ZERO : BigDecimal.valueOf(((long) s.xl4() + s.xl5()) * 5);
        BigDecimal big = paintball ? ZERO : BigDecimal.valueOf(((long) s.xl6() + s.xl7()) * 8);
        BigDecimal special = requestsTotal(job.specialRequests(), quantity);
        BigDecimal total = baseTotal.add(material).add(collar).add(sleeve).add(cutting).add(nameset)
                .add(piping).add(design).add(plus).add(big).add(special);
        return new InvoiceResponse.JobPricing(quantity, base, rate, baseTotal, material, collar, sleeve,
                cutting, nameset, piping, design, plus, big, special, total);
    }

    private BigDecimal perPiece(String category, String option, long quantity) {
        return PricingCatalog.option(category, option).multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal requestsTotal(List<InvoiceRequest.SpecialRequest> requests, long quantity) {
        if (requests == null) return ZERO;
        return requests.stream().map(request -> request.price().multiply(BigDecimal.valueOf(
                request.chargeType() == InvoiceRequest.ChargeType.PCS ? quantity : 1))).reduce(ZERO, BigDecimal::add);
    }
}
