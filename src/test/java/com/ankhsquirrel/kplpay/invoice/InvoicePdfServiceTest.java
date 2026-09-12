package com.ankhsquirrel.kplpay.invoice;

import com.ankhsquirrel.kplpay.customer.Customer;
import com.ankhsquirrel.kplpay.customer.CustomerNotFoundException;
import com.ankhsquirrel.kplpay.customer.CustomerRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoicePdfServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private InvoicePdfService service;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        // SpringTemplateEngine (not plain TemplateEngine) so the expression dialect is SpringEL,
        // matching production wiring, and not Thymeleaf's default OGNL dialect (an extra
        // dependency this project doesn't otherwise need).
        TemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        service = new InvoicePdfService(templateEngine, customerRepository);
    }

    private Invoice anInvoice(UUID customerId) {
        Invoice invoice = new Invoice(customerId, UUID.randomUUID(),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                new BigDecimal("29.99"), new BigDecimal("6.00"), new BigDecimal("35.99"));
        invoice.addLineItem(new LineItem("Growth — monthly subscription", 1,
                new BigDecimal("29.99"), new BigDecimal("29.99")));
        return invoice;
    }

    @Test
    void renders_a_readable_pdf_containing_customer_and_invoice_details() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer("55210055400122", "ACME SA", "1 rue Exemple", "75000", "Paris",
                "fin@acme.example");
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        byte[] pdf = service.generate(anInvoice(customerId));

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF");

        String text = extractText(pdf);
        assertThat(text).contains("ACME SA");
        assertThat(text).contains("Growth — monthly subscription");
        assertThat(text).contains("35.99");
    }

    @Test
    void propagates_customer_not_found_when_the_invoice_references_an_unknown_customer() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generate(anInvoice(customerId)))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    private static String extractText(byte[] pdf) {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(document);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
