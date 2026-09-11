-- Line items have no independent existence outside their invoice: ON DELETE CASCADE means
-- deleting an invoice deletes its line items. Mirrored on the JPA side by
-- Invoice#lineItems (cascade = ALL, orphanRemoval = true) — see Invoice.java for the reasoning.
CREATE TABLE line_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id UUID NOT NULL REFERENCES invoice(id) ON DELETE CASCADE,
    description VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price NUMERIC(10,2) NOT NULL,
    amount NUMERIC(10,2) NOT NULL
);

CREATE INDEX idx_line_item_invoice_id ON line_item (invoice_id);
