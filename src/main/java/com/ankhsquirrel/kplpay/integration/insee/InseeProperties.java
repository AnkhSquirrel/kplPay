package com.ankhsquirrel.kplpay.integration.insee;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the INSEE Sirene API client.
 *
 * @param baseUrl base URL of the Sirene V3.11 API, e.g. {@code https://api.insee.fr/entreprises/sirene/V3.11}
 * @param apiKey  static integration key from the INSEE developer portal (portail-api.insee.fr), sent in the
 *                {@code X-INSEE-Api-Key-Integration} header (never defaulted outside the local profile)
 */
@ConfigurationProperties(prefix = "insee")
public record InseeProperties(String baseUrl, String apiKey) {
}
