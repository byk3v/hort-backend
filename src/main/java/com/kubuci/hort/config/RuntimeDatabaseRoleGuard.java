package com.kubuci.hort.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RuntimeDatabaseRoleGuard implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void run(ApplicationArguments args) {
		DatabaseRole role = jdbcTemplate.queryForObject("""
			select current_user, rolsuper, rolbypassrls,
			  exists (
			    select 1 from pg_tables
			    where schemaname = 'hort' and tableowner = current_user
			  ) as owns_hort_tables
			from pg_roles
			where rolname = current_user
			""", (result, row) -> new DatabaseRole(result.getString("current_user"),
			result.getBoolean("rolsuper"), result.getBoolean("rolbypassrls"),
			result.getBoolean("owns_hort_tables")));

		if (role == null || role.superuser() || role.bypassRls() || role.ownsHortTables()) {
			String roleName = role == null ? "unknown" : role.name();
			throw new IllegalStateException("Unsafe PostgreSQL runtime role: " + roleName
				+ ". Runtime must not be superuser, BYPASSRLS, or own hort tables.");
		}
	}

	private record DatabaseRole(String name, boolean superuser, boolean bypassRls, boolean ownsHortTables) {}
}
