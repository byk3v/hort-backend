package com.kubuci.hort.collectors.api;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kubuci.hort.services.CollectorService;
import com.kubuci.hort.shared.api.PageResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/collectors")
@RequiredArgsConstructor
@Validated
public class CollectorV1Controller {

	private final CollectorService collectorService;

	@GetMapping
	@PreAuthorize("hasAnyRole('HORT_ADMIN', 'ASSISTANT')")
	public ResponseEntity<PageResponse<CollectorV1Dto>> list(
		@RequestParam(required = false) String name,
		@RequestParam(defaultValue = "0") @Min(0) int page,
		@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return ResponseEntity.ok(collectorService.listV1(name, page, size));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('HORT_ADMIN', 'ASSISTANT')")
	public ResponseEntity<CollectorV1Dto> getById(@PathVariable UUID id) {
		return ResponseEntity.ok(collectorService.getV1ById(id));
	}

	@PostMapping
	@PreAuthorize("hasRole('HORT_ADMIN')")
	public ResponseEntity<CollectorV1Dto> create(@Valid @RequestBody CollectorWriteRequest request) {
		CollectorV1Dto created = collectorService.createV1(request);
		return ResponseEntity.created(URI.create("/api/v1/collectors/" + created.id()))
			.body(created);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('HORT_ADMIN')")
	public ResponseEntity<CollectorV1Dto> update(@PathVariable UUID id,
		@Valid @RequestBody CollectorWriteRequest request) {
		return ResponseEntity.ok(collectorService.updateV1(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('HORT_ADMIN')")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		collectorService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
