package com.ankhsquirrel.kplpay.subscription;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API view of a {@link Subscription}. Controllers never expose the JPA entity directly.
 */
public record SubscriptionResponse(
        UUID id,
        UUID customerId,
        String planName,
        BigDecimal monthlyAmount,
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
