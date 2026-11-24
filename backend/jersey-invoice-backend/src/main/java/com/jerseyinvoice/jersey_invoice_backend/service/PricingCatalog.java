package com.jerseyinvoice.jersey_invoice_backend.service;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** The server-owned catalog for selectable product options. */
final class PricingCatalog {
    private static final Map<String, BigDecimal> MATERIALS = Map.ofEntries(
            Map.entry("Eyelet 150GSM", money("0")),
            Map.entry("Spandex 260GSM", money("0")),
            Map.entry("RJ 170GSM", money("0")),
            Map.entry("Mini Eyelet 155GSM", money("0")),
            Map.entry("Honeycomb 150GSM", money("0")),
            Map.entry("Diamond 155GSM (+RM2)", money("2")),
            Map.entry("Micro Mesh 160GSM (+RM2)", money("2")),
            Map.entry("Diamond Mesh 160GSM (+RM2)", money("2")),
            Map.entry("Lattice 145GSM (+RM2)", money("2")),
            Map.entry("UA Hexagon 165GSM (+RM2)", money("2")),
            Map.entry("Mini Mesh 160GSM (+RM2)", money("2")),
            Map.entry("UMC Jacquard 160GSM (+RM2)", money("2")),
            Map.entry("Apple Mesh 155GSM (+RM4)", money("4")),
            Map.entry("Pique 165GSM (+RM5)", money("5")),
            Map.entry("Baseball 230GSM (+RM8)", money("8")),
            Map.entry("Jacquard 9 150GSM (+RM10)", money("10")),
            Map.entry("Sandwich 250GSM (+RM10)", money("10")),
            Map.entry("Computer Jacquard 170GSM (+RM12)", money("12")),
            Map.entry("Lycra 280GSM (+RM15)", money("15")));
    private static final Map<String, BigDecimal> COLLARS = Map.ofEntries(
            Map.entry("Roundneck", money("0")),
            Map.entry("V Neck Overlap", money("0")),
            Map.entry("V Neck Centre", money("0")),
            Map.entry("Roundneck Overlap (+RM2)", money("2")),
            Map.entry("Roundneck Insert (+RM2)", money("2")),
            Map.entry("V Neck No End (+RM2)", money("2")),
            Map.entry("V Neck No End Overlap (+RM2)", money("2")),
            Map.entry("No End NBA (+RM2)", money("2")),
            Map.entry("V Neck NFL (+RM3)", money("3")),
            Map.entry("Swoosh Classic (+RM3)", money("3")),
            Map.entry("V Neck No End NFL (+RM4)", money("4")),
            Map.entry("No End Insert (+RM4)", money("4")),
            Map.entry("Open Stub (+RM4)", money("4")),
            Map.entry("V Neck Stub (+RM5)", money("5")),
            Map.entry("Henley Button (+RM5)", money("5")),
            Map.entry("Polo Button (+RM5)", money("5")),
            Map.entry("Polo Boxflat (+RM5)", money("5")),
            Map.entry("Mandarin Button (+RM5)", money("5")),
            Map.entry("Mandarin Hidden Button (+RM5)", money("5")),
            Map.entry("Polo Retro NFL (+RM6)", money("6")),
            Map.entry("Polo Retro (+RM6)", money("6")),
            Map.entry("Retro No End (+RM6)", money("6")),
            Map.entry("Retro Overlap (+RM6)", money("6")),
            Map.entry("Mandarin Zip (+RM6)", money("6")),
            Map.entry("Polo Zip (+RM8)", money("8")),
            Map.entry("Retro V Line (+RM8)", money("8")),
            Map.entry("Retro Insert (+RM8)", money("8")),
            Map.entry("Retro No End NFL (+RM8)", money("8")),
            Map.entry("Retro Overlap Insert (+RM8)", money("8")));
    private static final Map<String, BigDecimal> SLEEVES = Map.ofEntries(
            Map.entry("Short Sleeve", money("0")),
            Map.entry("Long Sleeve (+RM3)", money("3")),
            Map.entry("Sleeveless (-RM2)", money("-2")),
            Map.entry("Extended Sleeve (+RM1)", money("1")));
    private static final Map<String, BigDecimal> NAMESETS = Map.ofEntries(
            Map.entry("None", money("0")),
            Map.entry("Add Number (+RM3)", money("3")),
            Map.entry("Add Name (+RM3)", money("3")),
            Map.entry("Add Name+Number (+RM5)", money("5")),
            Map.entry("Add Name+Number (Front+Back) (+RM6)", money("6")));
    private static final Map<String, BigDecimal> CUTTINGS = Map.ofEntries(
            Map.entry("Standard", money("0")),
            Map.entry("Raglan", money("0")),
            Map.entry("NFL (+RM10)", money("10")));
    private static final Map<String, BigDecimal> PIPING = Map.ofEntries(
            Map.entry("None", money("0")),
            Map.entry("Type A (+RM9)", money("9")),
            Map.entry("Type B (+RM9)", money("9")),
            Map.entry("Type C (+RM9)", money("9")),
            Map.entry("Type D (+RM5)", money("5")));

    private PricingCatalog() {}
    private static BigDecimal money(String value) { return new BigDecimal(value); }
    static BigDecimal option(String category, String value) {
        Map<String, BigDecimal> options = switch (category) {
            case "material" -> MATERIALS;
            case "collar" -> COLLARS;
            case "sleeve" -> SLEEVES;
            case "nameset" -> NAMESETS;
            case "cutting" -> CUTTINGS;
            case "piping" -> PIPING;
            case "design" -> Map.of("Customer Provided", money("0"), "Company Template", money("0"), "Custom Design (+RM50)", money("50"));
            default -> throw new IllegalArgumentException("Unknown option category");
        };
        BigDecimal amount = options.get(value);
        if (amount == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + category + " option");
        return amount;
    }
}
