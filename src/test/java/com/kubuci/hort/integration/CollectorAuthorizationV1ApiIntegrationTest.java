package com.kubuci.hort.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class CollectorAuthorizationV1ApiIntegrationTest extends PostgresIntegrationTest {

	private static final String HORT_1 = "11111111-1111-1111-1111-111111111111";
	private static final String HORT_1_STUDENT = "cccccccc-cccc-cccc-cccc-ccccccccccc1";
	private static final String HORT_1_COLLECTOR = "dddddddd-dddd-dddd-dddd-ddddddddddd1";
	private static final String HORT_2_COLLECTOR = "22222222-dddd-dddd-dddd-ddddddddddd1";

	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper objectMapper;
	@Autowired private JdbcTemplate jdbcTemplate;
	@Autowired private TransactionTemplate transactionTemplate;

	@Test
	void collectorCatalogIsPagedTenantScopedAndRoleProtected() throws Exception {
		mockMvc.perform(get("/api/v1/collectors").with(user("ASSISTANT")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items", hasSize(1)))
			.andExpect(jsonPath("$.items[0].id").value(HORT_1_COLLECTOR))
			.andExpect(jsonPath("$.totalElements").value(1));

		mockMvc.perform(get("/api/v1/collectors").with(user("PARENT")))
			.andExpect(status().isForbidden());
}

	@Test
	void exposesUnifiedActiveAuthorizationsWithoutCrossTenantData() throws Exception {
		mockMvc.perform(get("/api/v1/student-authorizations")
			.param("status", "ACTIVE")
			.with(user("ASSISTANT")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items", hasSize(1)))
			.andExpect(jsonPath("$.items[0].kind").value("PICKUP_RIGHT"))
			.andExpect(jsonPath("$.items[0].collector.id").value(HORT_1_COLLECTOR))
			.andExpect(jsonPath("$.items[0].validFrom").value("2026-01-01T00:00:00Z"));

		mockMvc.perform(get("/api/v1/student-authorizations").with(user("PARENT")))
			.andExpect(status().isForbidden());
	}

	@Test
	void derivesScheduledThenPersistsIdempotentRevocation() throws Exception {
		JsonNode created = create("""
			{
			 "kind":"PICKUP_RIGHT","studentId":"%s","duration":"DAILY",
			 "validFrom":"2099-01-01T10:00:00Z","validUntil":"2099-01-01T18:00:00Z",
			 "collector":{"source":"EXISTING","existingCollectorId":"%s"}
			}
			""".formatted(HORT_1_STUDENT, HORT_1_COLLECTOR));
		UUID id = UUID.fromString(created.get("id").asText());
		org.assertj.core.api.Assertions.assertThat(created.get("status").asText()).isEqualTo("SCHEDULED");

		mockMvc.perform(put("/api/v1/student-authorizations/PICKUP_RIGHT/{id}/revoke", id)
			.with(user("ASSISTANT"))).andExpect(status().isNoContent());
		mockMvc.perform(put("/api/v1/student-authorizations/PICKUP_RIGHT/{id}/revoke", id)
			.with(user("ASSISTANT"))).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/v1/student-authorizations/PICKUP_RIGHT/{id}", id)
			.with(user("HORT_ADMIN")))
			.andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REVOKED"));
		cleanupPickup(id);
	}

	@Test
	void createsPermanentSelfDismissalWithWeeklyRules() throws Exception {
		JsonNode created = create("""
			{
			 "kind":"SELF_DISMISSAL","studentId":"%s","duration":"PERMANENT",
			 "validFrom":"2026-01-01T00:00:00Z","validUntil":null,
			 "weeklyRules":[
			   {"dayOfWeek":"MONDAY","allowedFromTime":"15:30:00"},
			   {"dayOfWeek":"FRIDAY","allowedFromTime":"14:00:00"}
			 ]
			}
			""".formatted(HORT_1_STUDENT));
		UUID id = UUID.fromString(created.get("id").asText());
		org.assertj.core.api.Assertions.assertThat(created.get("weeklyRules")).hasSize(2);
		org.assertj.core.api.Assertions.assertThat(created.get("status").asText()).isEqualTo("ACTIVE");
		cleanupDismissal(id);
	}

	@Test
	void validatesAuthorizationShapeAndRollsBackCrossTenantCollector() throws Exception {
		mockMvc.perform(post("/api/v1/student-authorizations").with(user("HORT_ADMIN"))
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
				{"kind":"SELF_DISMISSAL","studentId":"%s","duration":"PERMANENT",
				 "validFrom":"2026-01-01T00:00:00Z","weeklyRules":[]}
				""".formatted(HORT_1_STUDENT)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("validation_failed"));

		mockMvc.perform(post("/api/v1/student-authorizations").with(user("HORT_ADMIN"))
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
				{"kind":"PICKUP_RIGHT","studentId":"%s","duration":"PERMANENT",
				 "validFrom":"2026-01-01T00:00:00Z",
				 "collector":{"source":"EXISTING","existingCollectorId":"%s"}}
				""".formatted(HORT_1_STUDENT, HORT_2_COLLECTOR)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("resource_not_found"));
	}

	@Test
	void unknownRouteUsesProblemNotFoundInsteadOfInternalError() throws Exception {
		mockMvc.perform(get("/api/permissions").with(user("HORT_ADMIN")))
			.andExpect(status().isNotFound())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.code").value("resource_not_found"));
	}

	private JsonNode create(String body) throws Exception {
		String response = mockMvc.perform(post("/api/v1/student-authorizations")
			.with(user("HORT_ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(response);
	}

	private void cleanupPickup(UUID id) {
		inTenant(() -> jdbcTemplate.update("delete from hort.pickup_right where id = ?", id));
	}

	private void cleanupDismissal(UUID id) {
		inTenant(() -> {
			jdbcTemplate.update("delete from hort.self_dismissal_weekly_rule where self_dismissal_id = ?", id);
			jdbcTemplate.update("delete from hort.self_dismissal where id = ?", id);
		});
	}

	private void inTenant(Runnable operation) {
		transactionTemplate.executeWithoutResult(status -> {
			jdbcTemplate.queryForObject("select set_config('app.hort_id', ?, true)", String.class, HORT_1);
			operation.run();
		});
	}

	private JwtRequestPostProcessor user(String role) {
		return jwt().jwt(token -> token.subject("test-user").claim("hort_id", HORT_1))
			.authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
}
