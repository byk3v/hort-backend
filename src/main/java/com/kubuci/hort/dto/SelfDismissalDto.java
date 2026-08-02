package com.kubuci.hort.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.kubuci.hort.enums.PermissionStatus;

public record SelfDismissalDto(
	UUID id,
	UUID studentId,
	OffsetDateTime validFrom,
	OffsetDateTime validUntil,
	PermissionStatus status) {
}
