package com.ankhsquirrel.kplpay.integration.insee;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Builds the dedicated {@link RestClient} used to talk to the INSEE Sirene API.
 * Kept qualified ({@code inseeRestClient}) so it is never confused with clients added for other
 * integrations later.
 */
@Configuration
@EnableConfigurationProperties(InseeProperties.class)
class InseeClientConfig {

    @Bean
    RestClient inseeRestClient(InseeProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("X-INSEE-Api-Key-Integration", properties.apiKey())
                .requestFactory(requestFactory)
                .build();
    }
}
