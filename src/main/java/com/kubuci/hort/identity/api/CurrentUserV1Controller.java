package com.kubuci.hort.identity.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kubuci.hort.identity.application.CurrentUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class CurrentUserV1Controller {

	private final CurrentUserService currentUserService;

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public CurrentUserDto getCurrentUser() {
		return currentUserService.getCurrentUser();
	}
}
