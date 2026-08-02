package com.kubuci.hort.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.kubuci.hort.enums.PermissionStatus;
import com.kubuci.hort.enums.PermissionType;

public record PickupRightDto(
	UUID id,
	UUID studentId,
	UUID collectorId,
	PermissionType type,
	OffsetDateTime validFrom,
	OffsetDateTime validUntil,
	PermissionStatus status) {
}
