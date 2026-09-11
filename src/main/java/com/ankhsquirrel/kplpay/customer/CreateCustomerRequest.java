package com.ankhsquirrel.kplpay.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Payload for {@code POST /api/customers}. Everything else about the customer (legal name,
 * address) is resolved from the INSEE Sirene registry using the SIRET.
 */
public record CreateCustomerRequest(

        @Schema(description = "14-digit SIRET identifying the company in the INSEE Sirene registry",
                example = "44306184100047")
        @NotBlank
        @Pattern(regexp = "\\d{14}", message = "SIRET must be exactly 14 digits")
        String siret,

        @Schema(description = "Billing contact email for this customer", example = "billing@acme.fr")
        @NotBlank
        @Email
        String email) {
}
