package com.kubuci.hort.attendance.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kubuci.hort.attendance.application.AttendanceService;
import com.kubuci.hort.shared.api.PageResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAnyRole('HORT_ADMIN', 'ASSISTANT')")
public class AttendanceV1Controller {

	private final AttendanceService service;

	@GetMapping("/check-in-candidates")
	public ResponseEntity<PageResponse<AttendanceStudentDto>> candidates(
		@RequestParam(required = false) @Size(max = 100) String q,
		@RequestParam(defaultValue = "0") @Min(0) int page,
		@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return ResponseEntity.ok(service.checkInCandidates(q, page, size));
	}

	@PostMapping("/check-ins")
	public ResponseEntity<AttendanceSessionDto> checkIn(@Valid @RequestBody CheckInRequest request) {
		AttendanceSessionDto created = service.checkIn(request);
		return ResponseEntity.created(URI.create("/api/v1/attendance/sessions/" + created.id())).body(created);
	}

	@GetMapping("/present-students")
	public ResponseEntity<PageResponse<PresentStudentDto>> present(
		@RequestParam(required = false) @Size(max = 100) String q,
		@RequestParam(defaultValue = "0") @Min(0) int page,
		@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return ResponseEntity.ok(service.presentStudents(q, page, size));
	}

	@PostMapping("/check-outs")
	public ResponseEntity<AttendanceCheckOutDto> checkOut(
		@Valid @RequestBody AttendanceCheckOutRequest request) {
		AttendanceCheckOutDto created = service.checkOut(request);
		return ResponseEntity.created(URI.create("/api/v1/attendance/check-outs/" + created.id())).body(created);
	}
}
