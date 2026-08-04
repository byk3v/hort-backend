package com.kubuci.hort.identity.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kubuci.hort.identity.api.CurrentHortDto;
import com.kubuci.hort.identity.api.CurrentUserDto;
import com.kubuci.hort.security.TenantContext;
import com.kubuci.hort.security.TenantHortResolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

	private final TenantContext tenantContext;
	private final TenantHortResolver tenantHortResolver;

	@Transactional(readOnly = true)
	public CurrentUserDto getCurrentUser() {
		var hort = tenantHortResolver.requireCurrentHort();
		return new CurrentUserDto(tenantContext.requireUsername(), new CurrentHortDto(hort.getId(), hort.getName()));
	}
}
