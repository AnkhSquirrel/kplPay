package com.ankhsquirrel.kplpay.integration.insee;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InseeClientTest {

    private static final String SIRET = "55210055400122";
    private static final String API_KEY = "test-api-key";

    @RegisterExtension
    static final WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(options().dynamicPort())
            .build();

    private InseeClient client;

    @BeforeEach
    void setUp() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(2));

        RestClient restClient = RestClient.builder()
                .baseUrl(wireMock.baseUrl())
                .defaultHeader("X-INSEE-Api-Key-Integration", API_KEY)
                .requestFactory(requestFactory)
                .build();
        client = new InseeClient(restClient);
    }

    @Test
    void maps_a_successful_response_to_company_info() throws IOException {
        wireMock.stubFor(get(urlEqualTo("/siret/" + SIRET))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(sireneBody())));

        CompanyInfo info = client.lookup(SIRET);

        assertThat(info.legalName()).isEqualTo("AIR FRANCE");
        assertThat(info.address()).isEqualTo("45 RUE DE PARIS");
        assertThat(info.postalCode()).isEqualTo("95747");
        assertThat(info.city()).isEqualTo("ROISSY-EN-FRANCE");
    }

    @Test
    void sends_the_api_key_header() throws IOException {
        wireMock.stubFor(get(urlEqualTo("/siret/" + SIRET))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(sireneBody())));

        client.lookup(SIRET);

        wireMock.verify(getRequestedFor(urlEqualTo("/siret/" + SIRET))
                .withHeader("X-INSEE-Api-Key-Integration", equalTo(API_KEY)));
    }

    @Test
    void throws_SiretNotFoundException_on_404() {
        wireMock.stubFor(get(urlEqualTo("/siret/" + SIRET))
                .willReturn(aResponse().withStatus(404)));

        assertThatThrownBy(() -> client.lookup(SIRET))
                .isInstanceOf(SiretNotFoundException.class);
    }

    @Test
    void throws_InseeUnavailableException_on_500() {
        wireMock.stubFor(get(urlEqualTo("/siret/" + SIRET))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> client.lookup(SIRET))
                .isInstanceOf(InseeUnavailableException.class);
    }

    private static String sireneBody() throws IOException {
        return new ClassPathResource("insee/siret-response.json").getContentAsString(StandardCharsets.UTF_8);
    }
}
