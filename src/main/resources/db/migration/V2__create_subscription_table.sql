-- billing_cycle_day is capped at 28, NOT 31: a subscription billed on day 29-31 would have
-- no valid billing date in February. This is a deliberate domain constraint, not an oversight.
-- Do not "fix" it to 31.
CREATE TABLE subscription (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id       UUID NOT NULL REFERENCES customer(id),
    plan_name         VARCHAR(100) NOT NULL,
    monthly_amount    NUMERIC(10,2) NOT NULL CHECK (monthly_amount > 0),
    billing_cycle_day SMALLINT NOT NULL CHECK (billing_cycle_day BETWEEN 1 AND 28),
    status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'CANCELLED')),
    created_at        TIMESTAMP NOT NULL DEFAULT now()
);

-- Postgres does not auto-index foreign keys; invoice generation will filter subscriptions by customer.
CREATE INDEX idx_subscription_customer_id ON subscription (customer_id);
