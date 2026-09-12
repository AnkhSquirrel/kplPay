package com.ankhsquirrel.kplpay.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Pure calculation logic for turning a subscription's monthly amount into invoice numbers.
 * Deliberately framework-free (no Spring annotations) so it is trivially unit-testable; the
 * {@code invoice} package wires it up as a Spring bean via {@link InvoiceConfig}.
 *
 * <p>Every monetary value is BigDecimal, rounded HALF_UP to scale 2 at each step (not just the
 * final output), so rounding error can never silently compound across steps.
 */
public class InvoiceCalculationService {

    private static final int QUANTITY = 1; // one line item per invoice in this phase, always

    private final BigDecimal vatRate;
    private final int paymentTermsDays;

    public InvoiceCalculationService(BigDecimal vatRate, int paymentTermsDays) {
        this.vatRate = vatRate;
        this.paymentTermsDays = paymentTermsDays;
    }

    /**
     * @throws IllegalArgumentException if {@code monthlyAmount} is null, zero, or negative.
     *         This is the sole enforcement point for "no zero/negative-amount invoices" in this
     *         phase: the line_item table has no CHECK constraint on unit_price/amount, and there
     *         is no request-body DTO at the API boundary to attach Bean Validation to (the
     *         endpoint takes only a path variable).
     */
    public InvoiceCalculationResult calculate(String planName, BigDecimal monthlyAmount, LocalDate issueDate) {
        if (monthlyAmount == null || monthlyAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Line item amount must be greater than zero, got " + monthlyAmount);
        }

        BigDecimal unitPrice = round(monthlyAmount);
        BigDecimal lineAmount = round(unitPrice.multiply(BigDecimal.valueOf(QUANTITY)));

        // rounding is applied here too, even with a single line item, so behavior won't
        // change if this ever supports multiple line items.
        BigDecimal subtotal = round(lineAmount);
        BigDecimal taxAmount = round(subtotal.multiply(vatRate));
        BigDecimal totalAmount = round(subtotal.add(taxAmount));

        LocalDate dueDate = issueDate.plusDays(paymentTermsDays);

        LineItemCalculation lineItem = new LineItemCalculation(
                planName + " — monthly subscription", QUANTITY, unitPrice, lineAmount);

        return new InvoiceCalculationResult(
                issueDate, dueDate, subtotal, taxAmount, totalAmount, List.of(lineItem));
    }

    private static BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
