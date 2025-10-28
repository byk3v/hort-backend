package com.kubuci.hort.controllers;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.kubuci.hort.dto.NewPermissionRequest;
import com.kubuci.hort.dto.PermissionViewDto;
import com.kubuci.hort.dto.PickupRightDto;
import com.kubuci.hort.services.PickupRightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PickupRightController {

	private final PickupRightService service;

	@GetMapping
	public ResponseEntity<List<PermissionViewDto>> list(
		@RequestParam(name = "status", required = false, defaultValue = "ACTIVE") String status
	) {
		return ResponseEntity.ok(service.listPermissions(status));
	}

	@PostMapping
	public ResponseEntity<Void> create(@RequestBody @Valid NewPermissionRequest req) {
		service.createPermission(req);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PutMapping("/{id}/revoke")
	public ResponseEntity<Void> revoke(@PathVariable Long id) {
		service.revoke(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/by-student")
	public ResponseEntity<List<PickupRightDto>> byStudent(@RequestParam Long studentId) {
		return ResponseEntity.ok(service.listByStudent(studentId));
	}

	@GetMapping("/by-collector")
	public ResponseEntity<List<PickupRightDto>> byCollector(@RequestParam Long collectorId) {
		return ResponseEntity.ok(service.listByCollector(collectorId));
	}
}
