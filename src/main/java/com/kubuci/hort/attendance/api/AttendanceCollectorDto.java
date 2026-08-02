package com.kubuci.hort.attendance.api;

import java.time.LocalTime;
import java.util.UUID;

public record AttendanceCollectorDto(
	UUID collectorId,
	String firstName,
	String lastName,
	String phone,
	boolean mainCollector,
	LocalTime allowedFromTime,
	UUID pickupRightId) {
}
