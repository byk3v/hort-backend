package com.kubuci.hort.controllers;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kubuci.hort.dto.CollectorDto;
import com.kubuci.hort.dto.CollectorSaveRequest;
import com.kubuci.hort.dto.CollectorSaveWithPersonRequest;
import com.kubuci.hort.services.CollectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/collectors")
@RequiredArgsConstructor
public class CollectorController {
	private final CollectorService collectorService;

	@GetMapping
	public ResponseEntity<List<CollectorDto>> list() {
		return ResponseEntity.ok(collectorService.list());
	}

	@GetMapping("/{id}")
	public ResponseEntity<CollectorDto> getById(@PathVariable Long id) {
		return ResponseEntity.ok(collectorService.getById(id));
	}

	// Crea collector para una Person existente
	//@@PostMapping("/with-person")
	//public ResponseEntity<Long> create(@Valid @RequestBody CollectorSaveRequest req) {
	//	Long id = collectorService.create(req);
	//	return ResponseEntity.created(URI.create("/api/collectors/" + id)).body(id);
	//}

	//Crea collector + person en una sola llamada
	@PostMapping
	public ResponseEntity<Long> createWithPerson(@Valid @RequestBody CollectorSaveWithPersonRequest req) {
		Long id = collectorService.createWithPerson(req);
		return ResponseEntity.created(URI.create("/api/collectors/" + id)).body(id);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody CollectorSaveRequest req) {
		collectorService.update(id, req);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		collectorService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
