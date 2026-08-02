package com.kubuci.hort.integration;

import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
abstract class PostgresIntegrationTest {

	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
		.withDatabaseName("hortdb")
		.withUsername("hort_migrations")
		.withPassword("hort_migrations_test")
		.withInitScript("db/testcontainer/init.sql");

	static {
		POSTGRES.start();
	}

	@DynamicPropertySource
	static void databaseProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", () -> "hort_app");
		registry.add("spring.datasource.password", () -> "hort_app_test");
		registry.add("spring.datasource.hikari.maximum-pool-size", () -> "1");
		registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
		registry.add("spring.flyway.user", POSTGRES::getUsername);
		registry.add("spring.flyway.password", POSTGRES::getPassword);
		registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "http://issuer.test/realms/Hort");
		registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> "http://issuer.test/jwks");
	}

	@AfterEach
	void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}
}
