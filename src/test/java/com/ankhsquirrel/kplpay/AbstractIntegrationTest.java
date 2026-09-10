package com.ankhsquirrel.kplpay;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base class for tests that need the full application context backed by a real PostgreSQL.
 *
 * <p>The container is a singleton: started once on first class load and reused by every
 * integration test for the life of the JVM (Ryuk tears it down at exit). This keeps the
 * suite fast — spinning up Postgres per test class would dominate the runtime.
 *
 * <p>{@link ServiceConnection} wires the container's JDBC coordinates straight into the
 * Spring datasource, so no {@code spring.datasource.*} config is needed for the {@code test}
 * profile. Flyway then owns the schema, exactly as in production.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    static {
        POSTGRES.start();
    }
}
