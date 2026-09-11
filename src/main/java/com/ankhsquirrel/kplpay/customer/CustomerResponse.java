package com.ankhsquirrel.kplpay.customer;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API view of a {@link Customer}. Controllers never expose the JPA entity directly.
 */
public record CustomerResponse(

        @Schema(description = "Customer identifier")
        UUID id,

        @Schema(description = "14-digit SIRET identifying the company", example = "44306184100047")
        String siret,

        @Schema(description = "Legal company name, resolved from INSEE Sirene", example = "GOOGLE FRANCE")
        String companyName,

        @Schema(description = "Registered address, resolved from INSEE Sirene")
        String addressLine,

        String postalCode,

        String city,

        @Schema(description = "Billing contact email", example = "billing@acme.fr")
        String email,

        LocalDateTime createdAt) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getSiret(),
                customer.getCompanyName(),
                customer.getAddressLine(),
                customer.getPostalCode(),
                customer.getCity(),
                customer.getEmail(),
                customer.getCreatedAt());
    }
}
