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
import com.kubuci.hort.dto.SelfDismissalCreateRequest;
import com.kubuci.hort.dto.SelfDismissalDto;
import com.kubuci.hort.services.SelfDismissalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/self-dismissals")
@RequiredArgsConstructor
public class SelfDismissalController {
	private final SelfDismissalService service;

	@PostMapping
	public ResponseEntity<Long> create(@Valid @RequestBody SelfDismissalCreateRequest req) {
		Long id = service.create(req);
		return ResponseEntity.created(URI.create("/api/self-dismissals/" + id)).body(id);
	}

	@PutMapping("/{id}/revoke")
	public ResponseEntity<Void> revoke(@PathVariable Long id) {
		service.revoke(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<List<SelfDismissalDto>> listByStudent(@RequestParam Long studentId) {
		return ResponseEntity.ok(service.listByStudent(studentId));
	}
}
