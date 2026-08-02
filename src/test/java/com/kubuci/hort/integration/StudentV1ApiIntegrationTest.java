package com.kubuci.hort.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

class StudentV1ApiIntegrationTest extends PostgresIntegrationTest {

	private static final String HORT_1 = "11111111-1111-1111-1111-111111111111";
	private static final String HORT_1_GROUP = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2";
	private static final String HORT_1_COLLECTOR = "dddddddd-dddd-dddd-dddd-ddddddddddd1";
	private static final String HORT_2_STUDENT = "22222222-cccc-cccc-cccc-ccccccccccc1";
	private static final String HORT_2_COLLECTOR = "22222222-dddd-dddd-dddd-ddddddddddd1";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Test
	void returnsATenantScopedPageForAssistant() throws Exception {
		mockMvc.perform(get("/api/v1/students")
			.param("page", "0")
			.param("size", "2")
			.param("sort", "lastName,asc")
			.with(user(HORT_1, "ASSISTANT")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items", hasSize(2)))
			.andExpect(jsonPath("$.page").value(0))
			.andExpect(jsonPath("$.size").value(2))
			.andExpect(jsonPath("$.totalElements").value(9))
			.andExpect(jsonPath("$.totalPages").value(5));
	}

	@Test
	void filtersStudentsByNameAndGroup() throws Exception {
		mockMvc.perform(get("/api/v1/students")
			.param("name", "Anna")
			.param("groupId", HORT_1_GROUP)
			.with(user(HORT_1, "HORT_ADMIN")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items", hasSize(1)))
			.andExpect(jsonPath("$.items[0].id").value("cccccccc-cccc-cccc-cccc-ccccccccccc1"))
			.andExpect(jsonPath("$.items[0].group.id").value(HORT_1_GROUP));
	}

	@Test
	void enforcesTheStudentRoleMatrix() throws Exception {
		mockMvc.perform(get("/api/v1/students").with(user(HORT_1, "PARENT")))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("access_denied"));

		mockMvc.perform(post("/api/v1/students")
			.with(user(HORT_1, "ASSISTANT"))
			.contentType(MediaType.APPLICATION_JSON)
			.content(validExistingCollectorRequest("Assistant Cannot Create")))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("access_denied"));
	}

	@Test
	void validatesPaginationAndExactlyOneMainCollector() throws Exception {
		mockMvc.perform(get("/api/v1/students")
			.param("size", "101")
			.with(user(HORT_1, "HORT_ADMIN")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("validation_failed"));

		mockMvc.perform(post("/api/v1/students")
			.with(user(HORT_1, "HORT_ADMIN"))
			.contentType(MediaType.APPLICATION_JSON)
			.content(validExistingCollectorRequest("No Main").replace("\"mainCollector\":true",
				"\"mainCollector\":false")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("validation_failed"));

		mockMvc.perform(post("/api/v1/students")
			.with(user(HORT_1, "HORT_ADMIN"))
			.contentType(MediaType.APPLICATION_JSON)
			.content(newAndExistingCollectorsRequest("Two Main").replace(
				"\"permissionType\":\"PERMANENT\",\"mainCollector\":false",
				"\"permissionType\":\"PERMANENT\",\"mainCollector\":true")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("validation_failed"));
	}

	@Test
	void createsAStudentWithNewAndExistingCollectorsInOneAggregate() throws Exception {
		String response = mockMvc.perform(post("/api/v1/students")
			.with(user(HORT_1, "HORT_ADMIN"))
			.contentType(MediaType.APPLICATION_JSON)
			.content(newAndExistingCollectorsRequest("Versioned Student")))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/v1/students/.+")))
			.andExpect(jsonPath("$.id").isNotEmpty())
			.andExpect(jsonPath("$.group.id").value(HORT_1_GROUP))
			.andExpect(jsonPath("$.collectors", hasSize(2)))
			.andExpect(jsonPath("$.collectors[?(@.mainCollector == true)]", hasSize(1)))
			.andReturn()
			.getResponse()
			.getContentAsString();

		JsonNode created = objectMapper.readTree(response);
		UUID studentId = UUID.fromString(created.get("id").asText());
		UUID newCollectorId = null;
		for (JsonNode collector : created.get("collectors")) {
			if (collector.get("firstName").asText().equals("New")) {
				newCollectorId = UUID.fromString(collector.get("id").asText());
			}
		}
		if (newCollectorId == null) {
			throw new AssertionError("Created collector was not returned");
		}
		cleanupCreatedAggregate(studentId, newCollectorId);
	}

	@Test
	void rollsBackNewDataWhenAnExistingCollectorBelongsToAnotherTenant() throws Exception {
		mockMvc.perform(post("/api/v1/students")
			.with(user(HORT_1, "HORT_ADMIN"))
			.contentType(MediaType.APPLICATION_JSON)
			.content(newThenCrossTenantCollectorRequest()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("resource_not_found"));

		mockMvc.perform(get("/api/v1/students")
			.param("name", "Rollback Student")
			.with(user(HORT_1, "HORT_ADMIN")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items", hasSize(0)));
	}

	@Test
	void hidesCrossTenantAndUnknownStudentIdentifiers() throws Exception {
		mockMvc.perform(get("/api/v1/students/{id}", HORT_2_STUDENT).with(user(HORT_1, "HORT_ADMIN")))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("resource_not_found"));

		mockMvc.perform(get("/api/v1/students/{id}", UUID.randomUUID()).with(user(HORT_1, "HORT_ADMIN")))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("resource_not_found"));
	}

	@Test
	void preservesTheLegacyListWithTheSameReadAuthorization() throws Exception {
		mockMvc.perform(get("/api/students").with(user(HORT_1, "ASSISTANT")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(9)));

		mockMvc.perform(get("/api/students").with(user(HORT_1, "PARENT")))
			.andExpect(status().isForbidden());
	}

	private String validExistingCollectorRequest(String firstName) {
		return """
			{
			  "student":{"firstName":"%s","lastName":"Test","address":"Test address"},
			  "groupId":"%s",
			  "canLeaveAlone":false,
			  "collectors":[{
			    "source":"EXISTING",
			    "existingCollectorId":"%s",
			    "permissionType":"PERMANENT",
			    "mainCollector":true
			  }]
			}
			""".formatted(firstName, HORT_1_GROUP, HORT_1_COLLECTOR);
	}

	private String newAndExistingCollectorsRequest(String firstName) {
		return """
			{
			  "student":{"firstName":"%s","lastName":"Test","address":"Test address","phone":"100"},
			  "groupId":"%s",
			  "canLeaveAlone":false,
			  "collectors":[
			    {"source":"NEW","newCollector":{"firstName":"New","lastName":"Collector","phone":"200"},"permissionType":"PERMANENT","mainCollector":false},
			    {"source":"EXISTING","existingCollectorId":"%s","permissionType":"PERMANENT","mainCollector":true}
			  ]
			}
			""".formatted(firstName, HORT_1_GROUP, HORT_1_COLLECTOR);
	}

	private String newThenCrossTenantCollectorRequest() {
		return """
			{
			  "student":{"firstName":"Rollback Student","lastName":"Test"},
			  "groupId":"%s",
			  "canLeaveAlone":false,
			  "collectors":[
			    {"source":"NEW","newCollector":{"firstName":"Rolled","lastName":"Back"},"permissionType":"PERMANENT","mainCollector":true},
			    {"source":"EXISTING","existingCollectorId":"%s","permissionType":"PERMANENT","mainCollector":false}
			  ]
			}
			""".formatted(HORT_1_GROUP, HORT_2_COLLECTOR);
	}

	private void cleanupCreatedAggregate(UUID studentId, UUID newCollectorId) {
		transactionTemplate.executeWithoutResult(status -> {
			jdbcTemplate.queryForObject("select set_config('app.hort_id', ?, true)", String.class, HORT_1);
			UUID studentPerson = jdbcTemplate.queryForObject("select person_id from hort.student where id = ?",
				UUID.class, studentId);
			UUID collectorPerson = jdbcTemplate.queryForObject("select person_id from hort.collector where id = ?",
				UUID.class, newCollectorId);
			jdbcTemplate.update("delete from hort.pickup_right where student_id = ?", studentId);
			jdbcTemplate.update("delete from hort.student where id = ?", studentId);
			jdbcTemplate.update("delete from hort.collector where id = ?", newCollectorId);
			jdbcTemplate.update("delete from hort.person where id in (?, ?)", studentPerson, collectorPerson);
		});
	}

	private JwtRequestPostProcessor user(String hortId, String role) {
		return jwt().jwt(token -> token.subject("test-user")
			.claim("hort_id", hortId))
			.authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
}
