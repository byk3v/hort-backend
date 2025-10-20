package com.kubuci.hort.dto;

import java.time.LocalDateTime;
import com.kubuci.hort.enums.PermissionStatus;
import com.kubuci.hort.enums.PermissionType;

public record PickupRightDto(
	Long id,
	Long studentId,
	Long collectorId,
	PermissionType type,
	LocalDateTime validFrom,
	LocalDateTime validUntil,
	PermissionStatus status
) {}
