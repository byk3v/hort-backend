package com.kubuci.hort.groups.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kubuci.hort.dto.GroupDto;
import com.kubuci.hort.dto.GroupSaveRequest;
import com.kubuci.hort.dto.GroupUpdateRequest;
import com.kubuci.hort.services.GroupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupV1Controller {

	private final GroupService groupService;

	@GetMapping
	@PreAuthorize("hasAnyRole('HORT_ADMIN', 'ASSISTANT')")
	public ResponseEntity<List<GroupDto>> list() {
		return ResponseEntity.ok(groupService.list());
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('HORT_ADMIN', 'ASSISTANT')")
	public ResponseEntity<GroupDto> getById(@PathVariable UUID id) {
		return ResponseEntity.ok(groupService.getById(id));
	}

	@PostMapping
	@PreAuthorize("hasRole('HORT_ADMIN')")
	public ResponseEntity<GroupDto> create(@Valid @RequestBody GroupSaveRequest request) {
		GroupDto created = groupService.create(request);
		return ResponseEntity.created(URI.create("/api/v1/groups/" + created.id()))
			.body(created);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('HORT_ADMIN')")
	public ResponseEntity<Void> update(@PathVariable UUID id, @Valid @RequestBody GroupUpdateRequest request) {
		groupService.update(id, request);
		return ResponseEntity.noContent()
			.build();
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('HORT_ADMIN')")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		groupService.delete(id);
		return ResponseEntity.noContent()
			.build();
	}
}
