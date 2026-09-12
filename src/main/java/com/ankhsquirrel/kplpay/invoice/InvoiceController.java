package com.ankhsquirrel.kplpay.invoice;

import com.ankhsquirrel.kplpay.common.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Invoices", description = "Invoice generation with VAT calculation")
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    @Operation(summary = "Generate an invoice for a subscription",
            description = "Generates a draft invoice for an active subscription's current "
                    + "billing period, including VAT calculation.")

    @ApiResponse(responseCode = "201", description = "Invoice created",
            content = @Content(schema = @Schema(implementation = InvoiceResponse.class)))
    @ApiResponse(responseCode = "400", description = "subscriptionId is not a well-formed UUID",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "No subscription exists with this id",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "The subscription is cancelled and can't be invoiced",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/generate/{subscriptionId}")
    public ResponseEntity<InvoiceResponse> generate(
            @Parameter(description = "Identifier of the subscription to invoice")
            @PathVariable UUID subscriptionId) {
        InvoiceResponse body = service.generate(subscriptionId);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/invoices/{id}")
                .buildAndExpand(body.id())
                .toUri();
        return ResponseEntity.created(location).body(body);
    }

    @Operation(summary = "Download an invoice as a PDF",
            description = "Renders the invoice — customer details, line items, and the "
                    + "subtotal/VAT/total breakdown — as a PDF document.")
    @ApiResponse(responseCode = "200", description = "PDF rendered",
            content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE,
                    schema = @Schema(type = "string", format = "binary")))
    @ApiResponse(responseCode = "400", description = "id is not a well-formed UUID",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "No invoice exists with this id",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(
            @Parameter(description = "Identifier of the invoice to render")
            @PathVariable UUID id) {
        byte[] pdf = service.generatePdf(id);

        ContentDisposition disposition = ContentDisposition.inline()
                .filename("invoice-" + id + ".pdf")
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(pdf);
    }
}
