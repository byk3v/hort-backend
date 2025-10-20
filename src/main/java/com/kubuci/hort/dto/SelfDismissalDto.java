package com.kubuci.hort.dto;

import java.time.LocalDateTime;
import com.kubuci.hort.enums.PermissionStatus;

public record SelfDismissalDto(
	Long id,
	Long studentId,
	LocalDateTime validFrom,
	LocalDateTime validUntil,
	PermissionStatus status
) {}
