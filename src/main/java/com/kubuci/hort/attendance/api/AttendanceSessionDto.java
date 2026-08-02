package com.kubuci.hort.attendance.api;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AttendanceSessionDto(
	UUID id,
	AttendanceStudentDto student,
	LocalDate attendanceDate,
	OffsetDateTime checkedInAt,
	String checkedInByUserId,
	String checkInComment,
	OffsetDateTime checkedOutAt,
	AttendanceStatus status) {
}
