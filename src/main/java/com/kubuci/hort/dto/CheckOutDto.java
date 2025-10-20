package com.kubuci.hort.dto;

import java.time.LocalDateTime;
import com.kubuci.hort.enums.CollectorType;

public record CheckOutDto(
	Long id,
	Long studentId,
	CollectorType collectorType,
	Long collectorId,
	LocalDateTime occurredAt,
	Long pickupRightId,
	Long selfDismissalId,
	String comment,
	String recordedByUserId
) {}
