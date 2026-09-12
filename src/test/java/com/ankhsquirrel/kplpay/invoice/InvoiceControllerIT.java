package com.ankhsquirrel.kplpay.invoice;

import com.ankhsquirrel.kplpay.AbstractIntegrationTest;
import com.ankhsquirrel.kplpay.customer.Customer;
import com.ankhsquirrel.kplpay.customer.CustomerRepository;
import com.ankhsquirrel.kplpay.subscription.Subscription;
import com.ankhsquirrel.kplpay.subscription.SubscriptionRepository;
import com.ankhsquirrel.kplpay.subscription.SubscriptionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class InvoiceControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private UUID anExistingCustomer() {
        Customer customer = customerRepository.save(
                new Customer("55210055400122", "ACME SA", null, null, null, "fin@acme.example"));
        return customer.getId();
    }

    private UUID anExistingSubscription(UUID customerId) {
        Subscription subscription = subscriptionRepository.save(
                new Subscription(customerId, "Growth", new BigDecimal("29.99"), 15));
        return subscription.getId();
    }

    private UUID aCancelledSubscription(UUID customerId) {
        Subscription subscription = new Subscription(customerId, "Growth", new BigDecimal("29.99"), 15);
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        return subscriptionRepository.save(subscription).getId();
    }

    @Test
    void generates_a_draft_invoice_for_an_active_subscription() throws Exception {
        UUID customerId = anExistingCustomer();
        UUID subscriptionId = anExistingSubscription(customerId);

        mockMvc.perform(post("/api/invoices/generate/{subscriptionId}", subscriptionId))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        matchesPattern(".*/api/invoices/[0-9a-f-]{36}")))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.subscriptionId").value(subscriptionId.toString()))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.subtotal").value(29.99))
                .andExpect(jsonPath("$.taxAmount").value(6.00))
                .andExpect(jsonPath("$.totalAmount").value(35.99))
                .andExpect(jsonPath("$.lineItems", hasSize(1)))
                .andExpect(jsonPath("$.lineItems[0].description").value("Growth — monthly subscription"))
                .andExpect(jsonPath("$.lineItems[0].quantity").value(1))
                .andExpect(jsonPath("$.lineItems[0].unitPrice").value(29.99))
                .andExpect(jsonPath("$.lineItems[0].amount").value(29.99));

        assertThat(invoiceRepository.findAll()).singleElement().satisfies(saved -> {
            assertThat(saved.getCustomerId()).isEqualTo(customerId);
            assertThat(saved.getSubscriptionId()).isEqualTo(subscriptionId);
            assertThat(saved.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
            assertThat(saved.getSubtotal()).isEqualByComparingTo("29.99");
            assertThat(saved.getTaxAmount()).isEqualByComparingTo("6.00");
            assertThat(saved.getTotalAmount()).isEqualByComparingTo("35.99");
            assertThat(saved.getDueDate()).isEqualTo(saved.getIssueDate().plusDays(30));
            assertThat(saved.getLineItems()).singleElement().satisfies(li ->
                    assertThat(li.getUnitPrice()).isEqualByComparingTo("29.99"));
        });
    }

    @Test
    void returns_404_when_subscription_does_not_exist() throws Exception {
        mockMvc.perform(post("/api/invoices/generate/{subscriptionId}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SUBSCRIPTION_NOT_FOUND"));

        assertThat(invoiceRepository.count()).isZero();
    }

    @Test
    void returns_409_when_subscription_is_cancelled() throws Exception {
        UUID customerId = anExistingCustomer();
        UUID subscriptionId = aCancelledSubscription(customerId);

        mockMvc.perform(post("/api/invoices/generate/{subscriptionId}", subscriptionId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SUBSCRIPTION_CANCELLED"));

        assertThat(invoiceRepository.count()).isZero();
    }

    @Test
    void returns_400_when_subscription_id_is_not_a_uuid() throws Exception {
        mockMvc.perform(post("/api/invoices/generate/{subscriptionId}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TYPE_MISMATCH"));

        assertThat(invoiceRepository.count()).isZero();
    }

    @Test
    void returns_a_readable_pdf_for_an_existing_invoice() throws Exception {
        UUID customerId = anExistingCustomer();
        UUID subscriptionId = anExistingSubscription(customerId);

        mockMvc.perform(post("/api/invoices/generate/{subscriptionId}", subscriptionId))
                .andExpect(status().isCreated());

        UUID invoiceId = invoiceRepository.findAll().getFirst().getId();

        MvcResult result = mockMvc.perform(get("/api/invoices/{id}/pdf", invoiceId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition",
                        containsString("invoice-" + invoiceId + ".pdf")))
                .andReturn();

        byte[] body = result.getResponse().getContentAsByteArray();
        assertThat(body).isNotEmpty();
        assertThat(new String(body, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    @Test
    void returns_404_when_invoice_does_not_exist_for_pdf() throws Exception {
        mockMvc.perform(get("/api/invoices/{id}/pdf", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INVOICE_NOT_FOUND"));
    }
}
