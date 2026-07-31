package com.kubuci.hort.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.kubuci.hort.models.Hort;
import com.kubuci.hort.repositories.HortRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TenantHortResolver {
	private final TenantContext tenantContext;
	private final HortRepository hortRepository;

	public Hort requireCurrentHort() {
		var hortId = tenantContext.requireHortId();
		return hortRepository.findById(hortId)
			.orElseThrow(() -> new AccessDeniedException("Unknown Hort"));
	}
}
