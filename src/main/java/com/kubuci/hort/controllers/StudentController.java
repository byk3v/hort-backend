package com.kubuci.hort.controllers;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.kubuci.hort.dto.StudentDto;
import com.kubuci.hort.dto.StudentSaveRequest;
import com.kubuci.hort.services.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

	private final StudentService studentService;

	// Lista + búsqueda por nombre y/o grupo
	// GET /api/students?name=ana&groupId=3
	@GetMapping
	public ResponseEntity<List<StudentDto>> list(
		@RequestParam(required = false) String name,
		@RequestParam(required = false) Long groupId
	) {
		return ResponseEntity.ok(studentService.list(name, groupId));
	}

	@PostMapping
	public ResponseEntity<Long> save(@Valid @RequestBody StudentSaveRequest req) {
		Long id = studentService.save(req);
		return ResponseEntity.ok(id);
	}
}
