package com.kubuci.hort.students.api;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kubuci.hort.services.StudentService;
import com.kubuci.hort.shared.api.PageResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Validated
public class StudentV1Controller {

	private final StudentService studentService;

	@GetMapping
	@PreAuthorize("hasAnyRole('HORT_ADMIN', 'ASSISTANT')")
	public ResponseEntity<PageResponse<StudentV1Dto>> list(
		@RequestParam(required = false) String name,
		@RequestParam(required = false) UUID groupId,
		@RequestParam(defaultValue = "0") @Min(0) int page,
		@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
		@RequestParam(defaultValue = "lastName,asc")
		@Pattern(regexp = "(lastName|firstName),(asc|desc)") String sort) {
		return ResponseEntity.ok(studentService.listV1(name, groupId, page, size, sort));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('HORT_ADMIN', 'ASSISTANT')")
	public ResponseEntity<StudentV1Dto> getById(@PathVariable UUID id) {
		return ResponseEntity.ok(studentService.getV1ById(id));
	}

	@PostMapping
	@PreAuthorize("hasRole('HORT_ADMIN')")
	public ResponseEntity<StudentV1Dto> create(@Valid @RequestBody StudentOnboardingV1Request request) {
		StudentV1Dto created = studentService.onboardV1(request);
		return ResponseEntity.created(URI.create("/api/v1/students/" + created.id()))
			.body(created);
	}
}
