package com.kubuci.hort.dto;

import java.time.LocalDateTime;

import com.kubuci.hort.enums.CollectorType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckOutCreateRequest(
	@NotNull Long studentId,
	Long collectorId,     // null si es self-dismissal
	Long pickupRightId,   // null si es self-dismissal
	Boolean selfDismissal, // true si se va solo, false si lo recoge alguien
	String comment
//	@NotBlank String recordedByUserId
) {}
