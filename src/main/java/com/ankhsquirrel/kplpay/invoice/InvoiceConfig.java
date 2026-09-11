package com.ankhsquirrel.kplpay.invoice;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the framework-free {@link InvoiceCalculationService} as a Spring bean, injecting
 * config-driven VAT rate and payment terms as plain constructor arguments (not a
 * {@link BillingProperties} object) so the service itself stays free of any Spring type.
 */
@Configuration
@EnableConfigurationProperties(BillingProperties.class)
class InvoiceConfig {

    @Bean
    InvoiceCalculationService invoiceCalculationService(BillingProperties properties) {
        return new InvoiceCalculationService(properties.vatRate(), properties.paymentTermsDays());
    }
}
