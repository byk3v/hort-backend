package com.kubuci.hort.attendance.api;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AttendanceCheckOutDto(
	UUID id,
	UUID attendanceId,
	UUID studentId,
	CheckoutMethod method,
	OffsetDateTime occurredAt,
	UUID collectorId,
	UUID pickupRightId,
	UUID selfDismissalId,
	String recordedByUserId,
	String comment) {
}
