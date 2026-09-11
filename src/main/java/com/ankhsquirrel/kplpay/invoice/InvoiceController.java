package com.ankhsquirrel.kplpay.invoice;

import com.ankhsquirrel.kplpay.common.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Invoice created",
                    content = @Content(schema = @Schema(implementation = InvoiceResponse.class))),
            @ApiResponse(responseCode = "400", description = "subscriptionId is not a well-formed UUID",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "No subscription exists with this id",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "The subscription is cancelled and can't be invoiced",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
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
}
