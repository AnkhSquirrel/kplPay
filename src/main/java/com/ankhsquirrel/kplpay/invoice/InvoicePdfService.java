package com.ankhsquirrel.kplpay.invoice;

import com.ankhsquirrel.kplpay.customer.Customer;
import com.ankhsquirrel.kplpay.customer.CustomerNotFoundException;
import com.ankhsquirrel.kplpay.customer.CustomerRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Renders an {@link Invoice} as a PDF document: Thymeleaf produces the HTML, openhtmltopdf turns
 * that HTML into PDF bytes.
 *
 * <p>{@code Invoice} only stores {@code customerId} (aggregate-boundary convention, see
 * {@link Invoice}'s javadoc), so this service fetches the {@link Customer} itself rather than
 * widening {@code Invoice} to carry a denormalized customer snapshot.
 */
@Service
public class InvoicePdfService {

    private static final String TEMPLATE_NAME = "invoice-template";

    private final TemplateEngine templateEngine;
    private final CustomerRepository customerRepository;

    public InvoicePdfService(TemplateEngine templateEngine, CustomerRepository customerRepository) {
        this.templateEngine = templateEngine;
        this.customerRepository = customerRepository;
    }

    public byte[] generate(Invoice invoice) {
        Customer customer = customerRepository.findById(invoice.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(invoice.getCustomerId()));

        Context context = new Context();
        context.setVariable("invoice", invoice);
        context.setVariable("customer", customer);
        String html = templateEngine.process(TEMPLATE_NAME, context);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to render invoice PDF for invoice " + invoice.getId(), e);
        }
    }
}
