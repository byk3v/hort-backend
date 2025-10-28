package com.kubuci.hort.controllers;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.kubuci.hort.dto.CheckOutCreateRequest;
import com.kubuci.hort.dto.CheckOutDto;
import com.kubuci.hort.dto.CheckOutSearchResponse;
import com.kubuci.hort.services.CheckOutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckOutController {
	private final CheckOutService service;

	@PostMapping("/confirm")
	public ResponseEntity<Void> create(@Valid @RequestBody CheckOutCreateRequest req) {
		service.registerCheckout(req);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@GetMapping("/search")
	public ResponseEntity<CheckOutSearchResponse> search(
		@RequestParam("q") String q
	) {
		if (q == null || q.trim().length() < 2) {
			return ResponseEntity.ok(new CheckOutSearchResponse(List.of()));
		}
		return ResponseEntity.ok(service.search(q.trim()));
	}

	@GetMapping("/by-student")
	public ResponseEntity<List<CheckOutDto>> byStudent(@RequestParam Long studentId) {
		return ResponseEntity.ok(service.listByStudent(studentId));
	}

	@GetMapping("/by-student-and-day")
	public ResponseEntity<List<CheckOutDto>> byStudentAndDay(@RequestParam Long studentId,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) {
		return ResponseEntity.ok(service.listByStudentAndDay(studentId, day));
	}
}
