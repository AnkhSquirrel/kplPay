-- Only DRAFT is produced by this phase's invoice generation. PENDING/PAID/OVERDUE exist in the
-- schema for forward compatibility with later billing/payment phases; no code path transitions
-- an invoice into them yet.
CREATE TABLE invoice (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customer(id),
    subscription_id UUID NOT NULL REFERENCES subscription(id),
    issue_date DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PENDING', 'PAID', 'OVERDUE')),
    subtotal NUMERIC(10,2) NOT NULL,
    tax_amount NUMERIC(10,2) NOT NULL,
    total_amount NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Postgres does not auto-index foreign keys. Both will be filtered on: invoice history per
-- customer, and (later) checking whether a subscription has already been invoiced this cycle.
CREATE INDEX idx_invoice_customer_id ON invoice (customer_id);
CREATE INDEX idx_invoice_subscription_id ON invoice (subscription_id);
