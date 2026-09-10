package com.ankhsquirrel.kplpay.customer;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API view of a {@link Customer}. Controllers never expose the JPA entity directly.
 */
public record CustomerResponse(
        UUID id,
        String siret,
        String companyName,
        String addressLine,
        String postalCode,
        String city,
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
