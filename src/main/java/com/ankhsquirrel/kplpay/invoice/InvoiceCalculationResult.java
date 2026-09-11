package com.ankhsquirrel.kplpay.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Result of {@link InvoiceCalculationService#calculate}, before persistence. */
public record InvoiceCalculationResult(
        LocalDate issueDate,
        LocalDate dueDate,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        List<LineItemCalculation> lineItems) {
}
