package com.kubuci.hort.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

class AttendanceV1ApiIntegrationTest extends PostgresIntegrationTest {

	private static final String HORT_1 = "11111111-1111-1111-1111-111111111111";
	private static final String STUDENT = "cccccccc-cccc-cccc-cccc-ccccccccccc1";
	private static final String COLLECTOR = "dddddddd-dddd-dddd-dddd-ddddddddddd1";
	private static final String PICKUP_RIGHT = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1";

	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;
	@Autowired JdbcTemplate jdbcTemplate;
	@Autowired TransactionTemplate transactionTemplate;

	@Test
	void candidatesAreTenantScopedAndParentIsForbidden() throws Exception {
		mockMvc.perform(get("/api/v1/attendance/check-in-candidates").param("q", "Anna")
			.with(user("ASSISTANT")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items", hasSize(1)))
			.andExpect(jsonPath("$.items[0].id").value(STUDENT));
		mockMvc.perform(get("/api/v1/attendance/check-in-candidates").with(user("PARENT")))
			.andExpect(status().isForbidden());
	}

	@Test
	void checkInCreatesPresenceAndRejectsSecondDailySession() throws Exception {
		JsonNode session = checkIn();
		UUID attendanceId = UUID.fromString(session.get("id").asText());
		try {
			mockMvc.perform(get("/api/v1/attendance/present-students").with(user("ASSISTANT")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].attendanceId").value(attendanceId.toString()))
				.andExpect(jsonPath("$.items[0].student.id").value(STUDENT));

			mockMvc.perform(post("/api/v1/attendance/check-ins").with(user("ASSISTANT"))
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"studentId":"%s"}
					""".formatted(STUDENT)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("attendance_already_checked_in"));
		}
		finally {
			cleanup(attendanceId);
		}
	}

	@Test
	void checkoutClosesPresenceAndCannotBeRepeatedOrCheckedInAgain() throws Exception {
		UUID attendanceId = UUID.fromString(checkIn().get("id").asText());
		UUID checkOutId = null;
		try {
			String response = mockMvc.perform(post("/api/v1/attendance/check-outs").with(user("HORT_ADMIN"))
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"attendanceId":"%s","method":"PICKUP","collectorId":"%s",
					 "pickupRightId":"%s","comment":"integration"}
					""".formatted(attendanceId, COLLECTOR, PICKUP_RIGHT)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.studentId").value(STUDENT))
				.andReturn().getResponse().getContentAsString();
			checkOutId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

			mockMvc.perform(get("/api/v1/attendance/present-students").with(user("ASSISTANT")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.items", hasSize(0)));
			mockMvc.perform(post("/api/v1/attendance/check-outs").with(user("ASSISTANT"))
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"attendanceId":"%s","method":"PICKUP","collectorId":"%s","pickupRightId":"%s"}
					""".formatted(attendanceId, COLLECTOR, PICKUP_RIGHT)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("attendance_already_checked_out"));
			mockMvc.perform(post("/api/v1/attendance/check-ins").with(user("ASSISTANT"))
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"studentId":"%s"}
					""".formatted(STUDENT)))
				.andExpect(status().isConflict());
		}
		finally {
			cleanup(attendanceId, checkOutId);
		}
	}

	@Test
	void validatesDiscriminatedCheckoutPayloadAndRequiresKnownAttendance() throws Exception {
		mockMvc.perform(post("/api/v1/attendance/check-outs").with(user("ASSISTANT"))
			.contentType(MediaType.APPLICATION_JSON).content("""
				{"attendanceId":"%s","method":"PICKUP"}
				""".formatted(UUID.randomUUID())))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("validation_failed"));

		mockMvc.perform(post("/api/v1/attendance/check-outs").with(user("ASSISTANT"))
			.contentType(MediaType.APPLICATION_JSON).content("""
				{"attendanceId":"%s","method":"SELF_DISMISSAL","selfDismissalId":"%s"}
				""".formatted(UUID.randomUUID(), UUID.randomUUID())))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("resource_not_found"));
	}

	@Test
	void scheduledAuthorizationCannotCheckoutAndLeavesSessionOpen() throws Exception {
		String authorizationResponse = mockMvc.perform(post("/api/v1/student-authorizations")
			.with(user("HORT_ADMIN")).contentType(MediaType.APPLICATION_JSON).content("""
				{"kind":"PICKUP_RIGHT","studentId":"%s","duration":"DAILY",
				 "validFrom":"2099-01-01T10:00:00Z","validUntil":"2099-01-01T18:00:00Z",
				 "collector":{"source":"EXISTING","existingCollectorId":"%s"}}
				""".formatted(STUDENT, COLLECTOR)))
			.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		UUID rightId = UUID.fromString(objectMapper.readTree(authorizationResponse).get("id").asText());
		UUID attendanceId = UUID.fromString(checkIn().get("id").asText());
		try {
			mockMvc.perform(post("/api/v1/attendance/check-outs").with(user("ASSISTANT"))
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"attendanceId":"%s","method":"PICKUP","collectorId":"%s","pickupRightId":"%s"}
					""".formatted(attendanceId, COLLECTOR, rightId)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("checkout_authorization_not_active"));
			mockMvc.perform(get("/api/v1/attendance/present-students").param("q", "Anna")
				.with(user("ASSISTANT")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.items", hasSize(1)));
		}
		finally {
			cleanup(attendanceId);
			inTenant(() -> jdbcTemplate.update("delete from hort.pickup_right where id = ?", rightId));
		}
	}

	private JsonNode checkIn() throws Exception {
		String response = mockMvc.perform(post("/api/v1/attendance/check-ins").with(user("ASSISTANT"))
			.contentType(MediaType.APPLICATION_JSON).content("""
				{"studentId":"%s","comment":"arrived"}
				""".formatted(STUDENT)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("PRESENT"))
			.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(response);
	}

	private void cleanup(UUID attendanceId, UUID... checkoutIds) {
		inTenant(() -> {
			jdbcTemplate.update("delete from hort.attendance_session where id = ?", attendanceId);
			for (UUID checkoutId : checkoutIds) {
				if (checkoutId != null) jdbcTemplate.update("delete from hort.check_out where id = ?", checkoutId);
			}
		});
	}

	private void inTenant(Runnable operation) {
		transactionTemplate.executeWithoutResult(status -> {
			jdbcTemplate.queryForObject("select set_config('app.hort_id', ?, true)", String.class, HORT_1);
			operation.run();
		});
	}

	private JwtRequestPostProcessor user(String role) {
		return jwt().jwt(token -> token.subject("attendance-test-user").claim("hort_id", HORT_1))
			.authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
}
