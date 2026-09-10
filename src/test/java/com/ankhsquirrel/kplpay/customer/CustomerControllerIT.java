package com.ankhsquirrel.kplpay.customer;

import com.ankhsquirrel.kplpay.AbstractIntegrationTest;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class CustomerControllerIT extends AbstractIntegrationTest {

    private static final String SIRET = "55210055400122";
    private static final String INSEE_KEY = "test-insee-key";

    private static final WireMockServer INSEE = new WireMockServer(options().dynamicPort());

    static {
        INSEE.start();
    }

    @DynamicPropertySource
    static void inseeProperties(DynamicPropertyRegistry registry) {
        registry.add("insee.base-url", INSEE::baseUrl);
        registry.add("insee.api-key", () -> INSEE_KEY);
    }

    @AfterAll
    static void stopWireMock() {
        INSEE.stop();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository repository;

    @BeforeEach
    void resetStubs() {
        INSEE.resetAll();
    }

    @Test
    void creates_a_customer_enriched_from_insee() throws Exception {
        INSEE.stubFor(get(urlEqualTo("/siret/" + SIRET))
                .willReturn(ok(sireneBody())));

        mockMvc.perform(post("/api/customers")
                        .contentType("application/json")
                        .content("""
                                {"siret":"%s","email":"ops@example.com"}
                                """.formatted(SIRET)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        matchesPattern(".*/api/customers/[0-9a-f-]{36}")))
                .andExpect(jsonPath("$.siret").value(SIRET))
                .andExpect(jsonPath("$.companyName").value("AIR FRANCE"))
                .andExpect(jsonPath("$.addressLine").value("45 RUE DE PARIS"))
                .andExpect(jsonPath("$.postalCode").value("95747"))
                .andExpect(jsonPath("$.city").value("ROISSY-EN-FRANCE"))
                .andExpect(jsonPath("$.email").value("ops@example.com"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        Customer persisted = repository.findBySiret(SIRET).orElseThrow();
        assertThat(persisted.getCompanyName()).isEqualTo("AIR FRANCE");
        assertThat(persisted.getCity()).isEqualTo("ROISSY-EN-FRANCE");

        INSEE.verify(getRequestedFor(urlEqualTo("/siret/" + SIRET))
                .withHeader("X-INSEE-Api-Key-Integration", equalTo(INSEE_KEY)));
    }

    @Test
    void rejects_a_siret_that_is_already_registered() throws Exception {
        repository.save(new Customer(SIRET, "EXISTING SA", null, null, null, "existing@example.com"));

        mockMvc.perform(post("/api/customers")
                        .contentType("application/json")
                        .content("""
                                {"siret":"%s","email":"ops@example.com"}
                                """.formatted(SIRET)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_SIRET"));

        INSEE.verify(0, getRequestedFor(urlEqualTo("/siret/" + SIRET)));
    }

    @Test
    void rejects_a_malformed_siret_and_email() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType("application/json")
                        .content("""
                                {"siret":"123","email":"not-an-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[*].field",
                        containsInAnyOrder("siret", "email")));

        assertThat(repository.count()).isZero();
    }

    @Test
    void returns_422_when_insee_has_no_match() throws Exception {
        INSEE.stubFor(get(urlEqualTo("/siret/" + SIRET))
                .willReturn(aResponse().withStatus(404)));

        mockMvc.perform(post("/api/customers")
                        .contentType("application/json")
                        .content("""
                                {"siret":"%s","email":"ops@example.com"}
                                """.formatted(SIRET)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("SIRET_NOT_FOUND"));

        assertThat(repository.findBySiret(SIRET)).isEmpty();
    }

    @Test
    void returns_503_when_insee_is_unavailable() throws Exception {
        INSEE.stubFor(get(urlEqualTo("/siret/" + SIRET))
                .willReturn(aResponse().withStatus(500)));

        mockMvc.perform(post("/api/customers")
                        .contentType("application/json")
                        .content("""
                                {"siret":"%s","email":"ops@example.com"}
                                """.formatted(SIRET)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("INSEE_UNAVAILABLE"));

        assertThat(repository.findBySiret(SIRET)).isEmpty();
    }

    private static ResponseDefinitionBuilder ok(String body) {
        return aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(body);
    }

    private static String sireneBody() throws IOException {
        return new ClassPathResource("insee/siret-response.json").getContentAsString(StandardCharsets.UTF_8);
    }
}
