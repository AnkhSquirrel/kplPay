package com.ankhsquirrel.kplpay.subscription;

/**
 * Lifecycle state of a {@link Subscription}. Persisted as a string (see the {@code status}
 * CHECK constraint in {@code V2__create_subscription_table.sql}), never as an ordinal.
 */
public enum SubscriptionStatus {
    ACTIVE,
    CANCELLED
}
