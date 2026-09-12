package com.ankhsquirrel.kplpay.invoice;

import lombok.Getter;

import java.io.Serial;
import java.util.UUID;

/**
 * Thrown when an operation references an invoice id that does not exist.
 */
@Getter
public class InvoiceNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID invoiceId;

    public InvoiceNotFoundException(UUID invoiceId) {
        super("No invoice found with id " + invoiceId);
        this.invoiceId = invoiceId;
    }

}
