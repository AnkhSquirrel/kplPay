package com.ankhsquirrel.kplpay.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Payload for {@code POST /api/customers}. Everything else about the customer (legal name,
 * address) is resolved from the INSEE Sirene registry using the SIRET.
 */
public record CreateCustomerRequest(

        @NotBlank
        @Pattern(regexp = "\\d{14}", message = "SIRET must be exactly 14 digits")
        String siret,

        @NotBlank
        @Email
        String email) {
}
