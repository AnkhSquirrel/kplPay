package com.ankhsquirrel.kplpay.invoice;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Billing configuration used to calculate invoices.
 *
 * @param vatRate          VAT rate applied to every invoice's subtotal, e.g. {@code 0.20} for 20%
 * @param paymentTermsDays days added to issue date to compute due date, e.g. {@code 30}
 */
@ConfigurationProperties(prefix = "billing")
public record BillingProperties(BigDecimal vatRate, int paymentTermsDays) {
}
