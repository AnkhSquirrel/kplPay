package com.ankhsquirrel.kplpay.invoice;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Billing configuration used to calculate invoices.
 *
 * <p>Note the {@code kplpay.} namespace here diverges from
 * {@link com.ankhsquirrel.kplpay.integration.insee.InseeProperties}'s flat {@code insee} prefix.
 * This matches the literal spec ({@code kplpay.billing.vat-rate}) rather than the existing
 * convention — a deliberate but slightly inconsistent choice, worth standardizing on one style
 * later.
 *
 * @param vatRate          VAT rate applied to every invoice's subtotal, e.g. {@code 0.20} for 20%
 * @param paymentTermsDays days added to issue date to compute due date, e.g. {@code 30}
 */
@ConfigurationProperties(prefix = "kplpay.billing")
public record BillingProperties(BigDecimal vatRate, int paymentTermsDays) {
}
