package com.kubuci.hort.attendance.api;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PresentStudentDto(
	UUID attendanceId,
	AttendanceStudentDto student,
	OffsetDateTime checkedInAt,
	boolean canLeaveAloneNow,
	LocalTime allowedToLeaveFromTime,
	UUID selfDismissalId,
	List<AttendanceCollectorDto> allowedCollectors) {
}
