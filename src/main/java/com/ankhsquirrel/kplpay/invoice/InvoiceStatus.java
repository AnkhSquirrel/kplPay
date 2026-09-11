package com.ankhsquirrel.kplpay.invoice;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle state of an invoice. Newly generated invoices start as DRAFT.")
public enum InvoiceStatus {
    DRAFT,
    PENDING,
    PAID,
    OVERDUE
}
