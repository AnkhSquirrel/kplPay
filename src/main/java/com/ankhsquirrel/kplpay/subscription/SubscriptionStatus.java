package com.ankhsquirrel.kplpay.subscription;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Lifecycle state of a {@link Subscription}. Persisted as a string (see the {@code status}
 * CHECK constraint in {@code V2__create_subscription_table.sql}), never as an ordinal.
 */
@Schema(description = "Lifecycle state of a subscription. ACTIVE subscriptions can generate "
        + "invoices; CANCELLED ones cannot.")
public enum SubscriptionStatus {
    ACTIVE,
    CANCELLED
}
