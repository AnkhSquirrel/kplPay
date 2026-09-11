package com.ankhsquirrel.kplpay.invoice;

import java.math.BigDecimal;

/** One computed line item, part of an {@link InvoiceCalculationResult}. */
public record LineItemCalculation(
        String description,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal amount) {
}
