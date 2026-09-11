package com.ankhsquirrel.kplpay.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payload for {@code POST /api/subscriptions}. Validation mirrors the DB constraints in
 * {@code V2__create_subscription_table.sql} so bad input fails with a 400 before it ever
 * reaches a database constraint violation.
 */
public record CreateSubscriptionRequest(

        @Schema(description = "Identifier of an existing customer")
        @NotNull
        UUID customerId,

        @Schema(description = "Name of the subscription plan", example = "Growth")
        @NotBlank
        @Size(max = 100)
        String planName,

        @Schema(description = "Recurring monthly amount, excluding VAT", example = "49.90")
        @NotNull
        @Positive
        @Digits(integer = 8, fraction = 2, message = "monthlyAmount must fit NUMERIC(10,2)")
        BigDecimal monthlyAmount,

        @Schema(description = "Day of the month invoices are generated on. 1-28 only, not "
                + "1-31, so every billing cycle is valid in every month including February.",
                example = "15", minimum = "1", maximum = "28")
        @NotNull
        @Min(1)
        @Max(28)
        Integer billingCycleDay) {
}
