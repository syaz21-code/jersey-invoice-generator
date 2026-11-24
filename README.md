# Jersey invoice generator

The browser form sends invoice details to a Spring Boot backend. The backend calculates pricing, creates the PDF with OpenPDF, saves it to disk and returns its URL and pricing breakdown. No database is required.

## Run

Requires Java 21. From `backend/jersey-invoice-backend`, start the backend with `./mvnw spring-boot:run` (or `mvnw.cmd spring-boot:run` on Windows). Serve the `frontend` directory with a local web server, such as VS Code Live Server, and open its `index.html`. The frontend uses `http://localhost:8080/api` by default; change `frontend/js/api.js` if the backend runs elsewhere.

Generated PDFs are saved under `./generated-invoice` relative to the backend process's working directory. When started from `backend/jersey-invoice-backend`, that means `backend/jersey-invoice-backend/generated-invoice`. Set `invoice.storage.directory` in `application.properties` to choose another directory. Keep this folder for invoices only and back it up.

## Pricing

The backend determines the base price per job from its quantity: RM37 for 1–10, RM32 for 11–20, RM26 for 21–60, RM24 for 61–100, and RM22 for 101 or more. Paintball adds RM60 per piece. Standard jobs incur RM5 per piece for 4XL–5XL and RM8 for 6XL–7XL. The server's product catalog defines the material, collar, sleeve, nameset, cutting, piping and design charges. A special request can be per piece (`PCS`) or one-time (`ONE`); postage and special-request prices are entered by the user. Money is calculated with `BigDecimal`.

## Invoices and API

The invoice number is `INV` followed by `100000 + the number of regular files in generated-invoice`. Creation is locked to coordinate concurrent requests. If the resulting name already exists, creation fails with HTTP 409 instead of overwriting the existing PDF. Deleting, moving or adding files can therefore cause a numbering conflict; restore the folder contents before continuing. The six-digit range ends at `INV999999`.

`POST /api/invoices` validates the input, generates the PDF, and returns HTTP 201 with its `number`, `pdfUrl` and a `pricing` breakdown. `GET /api/invoices/{number}/pdf` displays the saved PDF. The browser opens this URL and provides a link if a popup was blocked.

Example request:

```json
{
  "date": "2026-11-24",
  "postagePrice": null,
  "customer": { "name": "Ali", "companyName": null, "email": null,
                "phone": "0123456789", "address": "Kuala Lumpur" },
  "jobs": [{
    "jobType": "STANDARD", "jobName": "Team Jersey", "design": "Customer Provided",
    "sizes": { "s": 5, "m": 10, "l": 5 },
    "material": "Eyelet 150GSM", "collar": "Roundneck", "sleeve": "Short Sleeve",
    "nameset": "None", "cutting": "Standard", "piping": "None",
    "specialRequests": [{ "name": "Extra logo", "price": 5.00, "chargeType": "PCS" }]
  }],
  "specialRequests": []
}
```

Use `PAINTBALL` for paintball jobs. Size keys `xl2` through `xl7` represent 2XL through 7XL; omitted sizes default to zero. Special-request lists may be omitted or empty. The response contains the PDF and pricing only; customer and job data are not stored separately from the PDF.
