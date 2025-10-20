package com.kubuci.hort.dto;

import java.time.LocalDateTime;
import com.kubuci.hort.enums.CollectorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckOutCreateRequest(
	@NotNull Long studentId,
	@NotNull CollectorType collectorType,   // COLLECTOR o STUDENT
	Long collectorId,                       // requerido si COLLECTOR
	LocalDateTime occurredAt,               // si null, se usa now()
	String comment,
	@NotBlank String recordedByUserId
) {}
