package com.kubuci.hort.security;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class TenantContext {

	public static final String HORT_ID_CLAIM = "hort_id";

	public UUID requireHortId() {
		var jwtAuthentication = requireJwtAuthentication();

		String rawHortId = jwtAuthentication.getToken()
			.getClaimAsString(HORT_ID_CLAIM);
		if (rawHortId == null || rawHortId.isBlank()) {
			throw new AccessDeniedException("Missing required hort_id claim");
		}

		try {
			return UUID.fromString(rawHortId);
		}
		catch (IllegalArgumentException exception) {
			throw new AccessDeniedException("Invalid hort_id claim", exception);
		}
	}

	public String requireUserId() {
		String subject = requireJwtAuthentication().getToken()
			.getSubject();
		if (subject == null || subject.isBlank()) {
			throw new AccessDeniedException("Missing required sub claim");
		}
		return subject;
	}

	public String requireUsername() {
		String username = requireJwtAuthentication().getToken()
			.getClaimAsString("preferred_username");
		if (username == null || username.isBlank()) {
			throw new AccessDeniedException("Missing required preferred_username claim");
		}
		return username;
	}

	private JwtAuthenticationToken requireJwtAuthentication() {
		var authentication = SecurityContextHolder.getContext()
			.getAuthentication();
		if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
			return jwtAuthentication;
		}
		throw new AccessDeniedException("Authenticated JWT is required");
	}
}
