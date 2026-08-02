package com.kubuci.hort.attendance.api;

import java.util.UUID;

public record AttendanceStudentDto(UUID id, String firstName, String lastName, String groupName) {
}
