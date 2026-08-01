package com.kubuci.hort.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class TenantTransactionAspect {

	private final JdbcTemplate jdbcTemplate;
	private final TenantContext tenantContext;

	public TenantTransactionAspect(JdbcTemplate jdbcTemplate, TenantContext tenantContext) {
		this.jdbcTemplate = jdbcTemplate;
		this.tenantContext = tenantContext;
	}

	@Around("@annotation(org.springframework.transaction.annotation.Transactional)")
	public Object establishTenant(ProceedingJoinPoint joinPoint) throws Throwable {
		var hortId = tenantContext.requireHortId();
		jdbcTemplate.queryForObject("select set_config('app.hort_id', ?, true)", String.class, hortId.toString());
		Boolean knownHort = jdbcTemplate.queryForObject("select exists(select 1 from hort.hort where id = ?)",
			Boolean.class, hortId);
		if (!Boolean.TRUE.equals(knownHort)) {
			throw new AccessDeniedException("Unknown hort_id claim");
		}
		return joinPoint.proceed();
	}
}
