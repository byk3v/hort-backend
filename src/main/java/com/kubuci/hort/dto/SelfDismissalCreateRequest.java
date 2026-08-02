package com.kubuci.hort.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record SelfDismissalCreateRequest(
	@NotNull UUID studentId,
	@NotNull OffsetDateTime validFrom,
	OffsetDateTime validUntil) {
}
