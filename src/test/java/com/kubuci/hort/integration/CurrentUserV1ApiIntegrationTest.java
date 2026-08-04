package com.kubuci.hort.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

class CurrentUserV1ApiIntegrationTest extends PostgresIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void resolvesUsernameAndHortFromTheAuthenticatedTenant() throws Exception {
		mockMvc.perform(get("/api/v1/me")
			.with(jwt().jwt(token -> token.subject("admin-subject")
				.claim("preferred_username", "admin")
				.claim("hort_id", "11111111-1111-1111-1111-111111111111"))
				.authorities(new SimpleGrantedAuthority("ROLE_HORT_ADMIN"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.username").value("admin"))
			.andExpect(jsonPath("$.hort.id").value("11111111-1111-1111-1111-111111111111"))
			.andExpect(jsonPath("$.hort.name").value("Hort Demo Leipzig"));
	}

	@Test
	void rejectsMissingUsernameClaim() throws Exception {
		mockMvc.perform(get("/api/v1/me")
			.with(jwt().jwt(token -> token.subject("admin-subject")
				.claim("hort_id", "11111111-1111-1111-1111-111111111111"))
				.authorities(new SimpleGrantedAuthority("ROLE_HORT_ADMIN"))))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("access_denied"));
	}
}
