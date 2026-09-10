package com.ankhsquirrel.kplpay.subscription;

import com.ankhsquirrel.kplpay.AbstractIntegrationTest;
import com.ankhsquirrel.kplpay.customer.Customer;
import com.ankhsquirrel.kplpay.customer.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class SubscriptionControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private UUID anExistingCustomer() {
        Customer customer = customerRepository.save(
                new Customer("55210055400122", "ACME SA", null, null, null, "fin@acme.example"));
        return customer.getId();
    }

    @Test
    void creates_a_subscription_for_an_existing_customer() throws Exception {
        UUID customerId = anExistingCustomer();

        mockMvc.perform(post("/api/subscriptions")
                        .contentType("application/json")
                        .content("""
                                {"customerId":"%s","planName":"Growth","monthlyAmount":29.99,"billingCycleDay":15}
                                """.formatted(customerId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        matchesPattern(".*/api/subscriptions/[0-9a-f-]{36}")))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.planName").value("Growth"))
                .andExpect(jsonPath("$.monthlyAmount").value(29.99))
                .andExpect(jsonPath("$.billingCycleDay").value(15))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        assertThat(subscriptionRepository.findAll()).singleElement().satisfies(saved -> {
            assertThat(saved.getCustomerId()).isEqualTo(customerId);
            assertThat(saved.getPlanName()).isEqualTo("Growth");
            assertThat(saved.getMonthlyAmount()).isEqualByComparingTo("29.99");
            assertThat(saved.getBillingCycleDay()).isEqualTo(15);
            assertThat(saved.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        });
    }

    @Test
    void returns_404_when_customer_does_not_exist() throws Exception {
        mockMvc.perform(post("/api/subscriptions")
                        .contentType("application/json")
                        .content("""
                                {"customerId":"%s","planName":"Growth","monthlyAmount":29.99,"billingCycleDay":15}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"));

        assertThat(subscriptionRepository.count()).isZero();
    }

    @Test
    void rejects_a_non_positive_amount_and_out_of_range_cycle_day() throws Exception {
        UUID customerId = anExistingCustomer();

        mockMvc.perform(post("/api/subscriptions")
                        .contentType("application/json")
                        .content("""
                                {"customerId":"%s","planName":"Growth","monthlyAmount":0,"billingCycleDay":31}
                                """.formatted(customerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[*].field",
                        containsInAnyOrder("monthlyAmount", "billingCycleDay")));

        assertThat(subscriptionRepository.count()).isZero();
    }

    @Test
    void rejects_a_blank_plan_name() throws Exception {
        UUID customerId = anExistingCustomer();

        mockMvc.perform(post("/api/subscriptions")
                        .contentType("application/json")
                        .content("""
                                {"customerId":"%s","planName":"","monthlyAmount":29.99,"billingCycleDay":15}
                                """.formatted(customerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[*].field", hasItem("planName")));

        assertThat(subscriptionRepository.count()).isZero();
    }
}
