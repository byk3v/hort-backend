package com.kubuci.hort.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.kubuci.hort.enums.PermissionType;

import jakarta.validation.constraints.NotNull;

public record PickupRightCreateRequest(
	@NotNull UUID studentId,
	@NotNull UUID collectorId,
	@NotNull PermissionType type,
	@NotNull OffsetDateTime validFrom,
	OffsetDateTime validUntil) {
}
