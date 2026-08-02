package com.kubuci.hort.authorizations.api;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kubuci.hort.authorizations.application.StudentAuthorizationService;
import com.kubuci.hort.shared.api.PageResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/student-authorizations")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAnyRole('HORT_ADMIN', 'ASSISTANT')")
public class StudentAuthorizationV1Controller {

	private final StudentAuthorizationService service;

	@GetMapping
	public ResponseEntity<PageResponse<StudentAuthorizationDto>> list(
		@RequestParam(defaultValue = "ACTIVE")
		@Pattern(regexp = "ACTIVE|SCHEDULED|EXPIRED|REVOKED|ALL") String status,
		@RequestParam(required = false) UUID studentId,
		@RequestParam(defaultValue = "0") @Min(0) int page,
		@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return ResponseEntity.ok(service.list(status, studentId, page, size));
	}

	@GetMapping("/{kind}/{id}")
	public ResponseEntity<StudentAuthorizationDto> get(@PathVariable StudentAuthorizationKind kind,
		@PathVariable UUID id) {
		return ResponseEntity.ok(service.get(kind, id));
	}

	@PostMapping
	public ResponseEntity<StudentAuthorizationDto> create(
		@Valid @RequestBody StudentAuthorizationCreateRequest request) {
		StudentAuthorizationDto created = service.create(request);
		return ResponseEntity.created(URI.create("/api/v1/student-authorizations/" + created.kind() + "/"
			+ created.id())).body(created);
	}

	@PutMapping("/{kind}/{id}/revoke")
	public ResponseEntity<Void> revoke(@PathVariable StudentAuthorizationKind kind, @PathVariable UUID id) {
		service.revoke(kind, id);
		return ResponseEntity.noContent().build();
	}
}
