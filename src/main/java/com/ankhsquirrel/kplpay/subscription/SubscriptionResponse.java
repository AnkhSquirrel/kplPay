package com.ankhsquirrel.kplpay.subscription;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API view of a {@link Subscription}. Controllers never expose the JPA entity directly.
 */
public record SubscriptionResponse(

        @Schema(description = "Subscription identifier")
        UUID id,

        @Schema(description = "Identifier of the customer this subscription belongs to")
        UUID customerId,

        @Schema(description = "Name of the subscription plan", example = "Growth")
        String planName,

        @Schema(description = "Recurring monthly amount, excluding VAT", example = "49.90")
        BigDecimal monthlyAmount,

        @Schema(description = "Day of the month invoices are generated on (1-28)", example = "15")
        Integer billingCycleDay,

        SubscriptionStatus status,

        LocalDateTime createdAt) {

    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getCustomerId(),
                subscription.getPlanName(),
                subscription.getMonthlyAmount(),
                subscription.getBillingCycleDay(),
                subscription.getStatus(),
                subscription.getCreatedAt());
    }
}
