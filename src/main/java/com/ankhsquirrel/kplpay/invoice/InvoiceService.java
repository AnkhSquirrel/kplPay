package com.ankhsquirrel.kplpay.invoice;

import com.ankhsquirrel.kplpay.subscription.Subscription;
import com.ankhsquirrel.kplpay.subscription.SubscriptionCancelledException;
import com.ankhsquirrel.kplpay.subscription.SubscriptionNotFoundException;
import com.ankhsquirrel.kplpay.subscription.SubscriptionRepository;
import com.ankhsquirrel.kplpay.subscription.SubscriptionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class InvoiceService {

    private final InvoiceRepository repository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceCalculationService calculationService;
    private final InvoicePdfService pdfService;
    private final Clock clock;

    public InvoiceService(InvoiceRepository repository, SubscriptionRepository subscriptionRepository,
                           InvoiceCalculationService calculationService, InvoicePdfService pdfService,
                           Clock clock) {
        this.repository = repository;
        this.subscriptionRepository = subscriptionRepository;
        this.calculationService = calculationService;
        this.pdfService = pdfService;
        this.clock = clock;
    }

    @Transactional
    public InvoiceResponse generate(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(subscriptionId));

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new SubscriptionCancelledException(subscriptionId);
        }

        InvoiceCalculationResult result = calculationService.calculate(
                subscription.getPlanName(), subscription.getMonthlyAmount(), LocalDate.now(clock));

        Invoice invoice = new Invoice(
                subscription.getCustomerId(),
                subscription.getId(),
                result.issueDate(),
                result.dueDate(),
                result.subtotal(),
                result.taxAmount(),
                result.totalAmount());

        result.lineItems().forEach(li -> invoice.addLineItem(
                new LineItem(li.description(), li.quantity(), li.unitPrice(), li.amount())));

        // saveAndFlush so the INSERT (and cascaded line_item inserts) run now and
        // @CreationTimestamp is populated on the instance we map into the response.
        Invoice saved = repository.saveAndFlush(invoice);
        return InvoiceResponse.from(saved);
    }

    // Transactional (not just a repository lookup) because InvoicePdfService.generate() walks the
    // lazily-fetched lineItems association while rendering the template; the session must still
    // be open at that point.
    @Transactional(readOnly = true)
    public byte[] generatePdf(UUID id) {
        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        return pdfService.generate(invoice);
    }
}
