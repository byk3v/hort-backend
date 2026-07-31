package com.kubuci.hort.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class TenantContextTest {

	private final TenantContext tenantContext = new TenantContext();

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void resolvesHortAndUserFromAuthenticatedJwt() {
		UUID hortId = UUID.fromString("11111111-1111-1111-1111-111111111111");
		authenticate(Map.of("sub", "user-123", "hort_id", hortId.toString()));

		assertThat(tenantContext.requireHortId()).isEqualTo(hortId);
		assertThat(tenantContext.requireUserId()).isEqualTo("user-123");
	}

	@Test
	void rejectsMissingHortClaim() {
		authenticate(Map.of("sub", "user-123"));

		assertThatThrownBy(tenantContext::requireHortId)
			.isInstanceOf(AccessDeniedException.class)
			.hasMessage("Missing required hort_id claim");
	}

	@Test
	void rejectsMalformedHortClaim() {
		authenticate(Map.of("sub", "user-123", "hort_id", "not-a-uuid"));

		assertThatThrownBy(tenantContext::requireHortId)
			.isInstanceOf(AccessDeniedException.class)
			.hasMessage("Invalid hort_id claim");
	}

	@Test
	void rejectsNonJwtAuthentication() {
		assertThatThrownBy(tenantContext::requireHortId)
			.isInstanceOf(AccessDeniedException.class)
			.hasMessage("Authenticated JWT is required");
	}

	private void authenticate(Map<String, Object> claims) {
		var jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"), claims);
		SecurityContextHolder.getContext()
			.setAuthentication(new JwtAuthenticationToken(jwt));
	}
}
