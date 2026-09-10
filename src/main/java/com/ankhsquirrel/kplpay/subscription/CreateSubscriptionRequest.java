package com.ankhsquirrel.kplpay.subscription;

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

        @NotNull
        UUID customerId,

        @NotBlank
        @Size(max = 100)
        String planName,

        @NotNull
        @Positive
        @Digits(integer = 8, fraction = 2, message = "monthlyAmount must fit NUMERIC(10,2)")
        BigDecimal monthlyAmount,

        // 1-28, not 1-31: see the note in Subscription#billingCycleDay.
        @NotNull
        @Min(1)
        @Max(28)
        Integer billingCycleDay) {
}
