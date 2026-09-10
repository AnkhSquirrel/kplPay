package com.ankhsquirrel.kplpay;

import org.junit.jupiter.api.Test;

/**
 * Smoke test: the full application context starts against a real PostgreSQL. Catches broken
 * bean wiring, {@code @ConfigurationProperties} binding failures, and JPA mappings that don't
 * match the Flyway-managed schema.
 */
class KplpayApplicationIT extends AbstractIntegrationTest {

	@Test
	void contextLoads() {
	}

}
