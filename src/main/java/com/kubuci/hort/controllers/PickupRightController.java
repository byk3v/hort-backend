package com.kubuci.hort.controllers;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.kubuci.hort.dto.PickupRightCreateRequest;
import com.kubuci.hort.dto.PickupRightDto;
import com.kubuci.hort.services.PickupRightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pickup-rights")
@RequiredArgsConstructor
public class PickupRightController {
	private final PickupRightService service;

	@PostMapping
	public ResponseEntity<Long> create(@Valid @RequestBody PickupRightCreateRequest req) {
		Long id = service.create(req);
		return ResponseEntity.created(URI.create("/api/pickup-rights/" + id)).body(id);
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
