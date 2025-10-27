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

import com.kubuci.hort.dto.PersonDto;
import com.kubuci.hort.dto.PersonSaveRequest;
import com.kubuci.hort.dto.PersonUpdateRequest;
import com.kubuci.hort.services.PersonService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    // LIST
    @GetMapping
    public ResponseEntity<List<PersonDto>> list() {
        return ResponseEntity.ok(personService.list());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<PersonDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(personService.getById(id));
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Long> save(@Valid @RequestBody PersonSaveRequest req) {
        Long id = personService.save(req);
        return ResponseEntity.created(URI.create("/api/persons/" + id))
                .body(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody PersonUpdateRequest req) {
        personService.update(id, req);
        return ResponseEntity.noContent()
                .build();
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        personService.delete(id);
        return ResponseEntity.noContent()
                .build();
    }
}
