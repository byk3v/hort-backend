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

import com.kubuci.hort.dto.GroupDto;
import com.kubuci.hort.dto.GroupSaveRequest;
import com.kubuci.hort.dto.GroupUpdateRequest;
import com.kubuci.hort.services.GroupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    // lista
    @GetMapping
    public ResponseEntity<List<GroupDto>> list() {
        return ResponseEntity.ok(groupService.list());
    }

    // groupById
    @GetMapping("/{id}")
    public ResponseEntity<GroupDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getById(id));
    }

    // saveGroup (create)
    @PostMapping
    public ResponseEntity<Long> save(@Valid @RequestBody GroupSaveRequest req) {
        Long id = groupService.save(req);
        return ResponseEntity.created(URI.create("/api/groups/" + id))
                .body(id);
    }

    // update
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody GroupUpdateRequest req) {
        groupService.update(id, req);
        return ResponseEntity.noContent()
                .build();
    }

    // delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        groupService.delete(id);
        return ResponseEntity.noContent()
                .build();
    }
}
