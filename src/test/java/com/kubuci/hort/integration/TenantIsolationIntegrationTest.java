package com.kubuci.hort.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.support.TransactionTemplate;

import com.kubuci.hort.dto.GroupSaveRequest;
import com.kubuci.hort.dto.GroupUpdateRequest;
import com.kubuci.hort.services.CheckOutService;
import com.kubuci.hort.services.CollectorService;
import com.kubuci.hort.services.GroupService;
import com.kubuci.hort.services.PickupRightService;
import com.kubuci.hort.services.StudentService;

class TenantIsolationIntegrationTest extends PostgresIntegrationTest {

	private static final UUID HORT_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID HORT_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID HORT_2_GROUP = UUID.fromString("22222222-bbbb-bbbb-bbbb-bbbbbbbbbbb1");
	private static final UUID HORT_1_PERSON = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");
	private static final UUID HORT_2_STUDENT = UUID.fromString("22222222-cccc-cccc-cccc-ccccccccccc1");
	private static final UUID HORT_2_COLLECTOR = UUID.fromString("22222222-dddd-dddd-dddd-ddddddddddd1");

	@Autowired
	private GroupService groupService;

	@Autowired
	private StudentService studentService;

	@Autowired
	private CollectorService collectorService;

	@Autowired
	private PickupRightService pickupRightService;

	@Autowired
	private CheckOutService checkOutService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Test
	void reusesOneConnectionWithoutLeakingThePreviousTenant() {
		authenticate(HORT_1, "hort-1-admin");
		assertThat(groupService.list()).hasSize(12)
			.noneMatch(group -> group.id()
				.equals(HORT_2_GROUP));
		assertThat(currentTenantSetting()).isEmpty();

		authenticate(HORT_2, "hort-2-admin");
		assertThat(groupService.list()).singleElement()
			.satisfies(group -> {
				assertThat(group.id()).isEqualTo(HORT_2_GROUP);
				assertThat(group.name()).isEqualTo("Gruppe 1B");
			});
		assertThat(currentTenantSetting()).isEmpty();

		authenticate(HORT_1, "hort-1-admin");
		assertThat(groupService.list()).hasSize(12)
			.noneMatch(group -> group.id()
				.equals(HORT_2_GROUP));
	}

	@Test
	void isolatesGroupsStudentsCollectorsPermissionsAndCheckouts() {
		authenticate(HORT_1, "hort-1-admin");
		assertThat(studentService.list(null, null)).hasSize(9)
			.noneMatch(student -> student.id()
				.equals(HORT_2_STUDENT));
		assertThat(collectorService.list()).singleElement()
			.satisfies(collector -> assertThat(collector.id()).isNotEqualTo(HORT_2_COLLECTOR));
		assertThat(pickupRightService.listByStudent(HORT_2_STUDENT)).isEmpty();
		assertThat(checkOutService.listByStudent(HORT_2_STUDENT)).isEmpty();

		authenticate(HORT_2, "hort-2-admin");
		assertThat(studentService.list(null, null)).singleElement()
			.satisfies(student -> assertThat(student.id()).isEqualTo(HORT_2_STUDENT));
		assertThat(collectorService.list()).singleElement()
			.satisfies(collector -> assertThat(collector.id()).isEqualTo(HORT_2_COLLECTOR));
		assertThat(pickupRightService.listByStudent(HORT_2_STUDENT)).hasSize(1);
		assertThat(checkOutService.listByStudent(HORT_2_STUDENT)).hasSize(1);
	}

	@Test
	void hidesAnotherTenantIdentifierLikeAnUnknownIdentifier() {
		authenticate(HORT_1, "hort-1-admin");

		assertThatThrownBy(() -> groupService.getById(HORT_2_GROUP))
			.isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
			.hasMessage("Group not found: " + HORT_2_GROUP);
		assertThatThrownBy(() -> groupService.getById(UUID.fromString("99999999-9999-9999-9999-999999999999")))
			.isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
			.hasMessageStartingWith("Group not found:");
	}

	@Test
	void cannotUpdateOrDeleteAnotherTenantsGroup() {
		authenticate(HORT_1, "hort-1-admin");

		assertThatThrownBy(() -> groupService.update(HORT_2_GROUP, new GroupUpdateRequest("Compromised")))
			.isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
		assertThatThrownBy(() -> groupService.delete(HORT_2_GROUP))
			.isInstanceOf(jakarta.persistence.EntityNotFoundException.class);

		authenticate(HORT_2, "hort-2-admin");
		assertThat(groupService.getById(HORT_2_GROUP).name()).isEqualTo("Gruppe 1B");
	}

	@Test
	void rejectsAnUnknownTenantBeforeDomainQueriesRun() {
		authenticate(UUID.fromString("99999999-9999-9999-9999-999999999999"), "unknown-hort-user");

		assertThatThrownBy(groupService::list)
			.isInstanceOf(AccessDeniedException.class)
			.hasMessage("Unknown hort_id claim");
	}

	@Test
	void derivesTenantAndAuditUserWhenCreatingData() {
		authenticate(HORT_2, "hort-2-admin-subject");

		UUID groupId = groupService.save(new GroupSaveRequest("HORT-2 Created Group"));

		Map<String, Object> stored = transactionTemplate.execute(status -> {
			setTenant(HORT_2);
			return jdbcTemplate.queryForMap(
				"select hort_id, created_by from hort.hort_group where id = ?", groupId);
		});
		assertThat(stored).isNotNull();
		assertThat(stored.get("hort_id")).isEqualTo(HORT_2);
		assertThat(stored.get("created_by")).isEqualTo("hort-2-admin-subject");
	}

	@Test
	void databaseConstraintRejectsACrossTenantRelationship() {
		assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
			setTenant(HORT_1);
			jdbcTemplate.update("""
				insert into hort.student (id, hort_id, person_id, group_id, can_leave_alone)
				values (?, ?, ?, ?, false)
				""", UUID.randomUUID(), HORT_1, HORT_1_PERSON, HORT_2_GROUP);
		}))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void runtimeRoleCannotBypassRowLevelSecurity() {
		Map<String, Object> role = jdbcTemplate.queryForMap("""
			select current_user as role_name, rolsuper, rolbypassrls
			from pg_roles where rolname = current_user
			""");

		assertThat(role.get("role_name")).isEqualTo("hort_app");
		assertThat(role.get("rolsuper")).isEqualTo(false);
		assertThat(role.get("rolbypassrls")).isEqualTo(false);
	}

	private String currentTenantSetting() {
		return jdbcTemplate.queryForObject("select current_setting('app.hort_id', true)", String.class);
	}

	private void setTenant(UUID hortId) {
		jdbcTemplate.queryForObject("select set_config('app.hort_id', ?, true)", String.class, hortId.toString());
	}

	private void authenticate(UUID hortId, String subject) {
		var jwt = new Jwt("token", Instant.now(), Instant.now()
			.plusSeconds(60), Map.of("alg", "none"), Map.of("sub", subject, "hort_id", hortId.toString()));
		SecurityContextHolder.getContext()
			.setAuthentication(new JwtAuthenticationToken(jwt));
	}
}
