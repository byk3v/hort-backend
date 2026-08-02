package com.kubuci.hort.integration;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

class GroupV1ApiIntegrationTest extends PostgresIntegrationTest {

	private static final String HORT_1 = "11111111-1111-1111-1111-111111111111";
	private static final String HORT_2_GROUP = "22222222-bbbb-bbbb-bbbb-bbbbbbbbbbb1";
	private static final String REFERENCED_HORT_1_GROUP = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void returnsProblemDetailWhenAuthenticationIsMissing() throws Exception {
		mockMvc.perform(get("/api/v1/groups"))
			.andExpect(status().isUnauthorized())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.status").value(401))
			.andExpect(jsonPath("$.code").value("authentication_required"))
			.andExpect(jsonPath("$.traceId").isNotEmpty());
	}

	@Test
	void allowsAssistantToReadOnlyItsTenantGroups() throws Exception {
		mockMvc.perform(get("/api/v1/groups").with(user(HORT_1, "ASSISTANT")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].id", not(hasItem(HORT_2_GROUP))))
			.andExpect(jsonPath("$[*].name", hasItem("Gruppe 1B")));
	}

	@Test
	void deniesParentAccessToAdministrativeGroupCatalog() throws Exception {
		mockMvc.perform(get("/api/v1/groups").with(user(HORT_1, "PARENT")))
			.andExpect(status().isForbidden())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.code").value("access_denied"));
	}

	@Test
	void deniesAssistantGroupCreation() throws Exception {
		mockMvc.perform(post("/api/v1/groups")
			.with(user(HORT_1, "ASSISTANT"))
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"New Group\"}"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("access_denied"));
	}

	@Test
	void createsAGroupForAdminAndReturnsTheResource() throws Exception {
		String location = mockMvc.perform(post("/api/v1/groups")
			.with(user(HORT_1, "HORT_ADMIN"))
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"Phase 1 Group\"}"))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/v1/groups/.+")))
			.andExpect(jsonPath("$.id").isNotEmpty())
			.andExpect(jsonPath("$.name").value("Phase 1 Group"))
			.andReturn()
			.getResponse()
			.getHeader("Location");

		mockMvc.perform(delete(location).with(user(HORT_1, "HORT_ADMIN")))
			.andExpect(status().isNoContent());
	}

	@Test
	void returnsStructuredValidationErrors() throws Exception {
		mockMvc.perform(post("/api/v1/groups")
			.with(user(HORT_1, "HORT_ADMIN"))
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\" \"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("validation_failed"))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
	}

	@Test
	void hidesCrossTenantAndUnknownGroupIdsWithTheSameProblemCode() throws Exception {
		mockMvc.perform(get("/api/v1/groups/{id}", HORT_2_GROUP).with(user(HORT_1, "HORT_ADMIN")))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("resource_not_found"));

		mockMvc.perform(get("/api/v1/groups/{id}", UUID.randomUUID()).with(user(HORT_1, "HORT_ADMIN")))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("resource_not_found"));
	}

	@Test
	void mapsReferentialIntegrityFailuresToConflict() throws Exception {
		mockMvc.perform(delete("/api/v1/groups/{id}", REFERENCED_HORT_1_GROUP)
			.with(user(HORT_1, "HORT_ADMIN")))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("data_conflict"));
	}

	@Test
	void keepsTheLegacyReadPathButAppliesTheSameAuthorization() throws Exception {
		mockMvc.perform(get("/api/groups").with(user(HORT_1, "ASSISTANT")))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/groups").with(user(HORT_1, "PARENT")))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("access_denied"));
	}

	private JwtRequestPostProcessor user(String hortId, String role) {
		return jwt().jwt(token -> token.subject("test-user")
			.claim("hort_id", hortId))
			.authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
}
