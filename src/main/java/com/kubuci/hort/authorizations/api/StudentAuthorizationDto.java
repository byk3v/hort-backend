package com.kubuci.hort.authorizations.api;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.kubuci.hort.enums.PermissionType;

public record StudentAuthorizationDto(
	UUID id,
	StudentAuthorizationKind kind,
	AuthorizationStudentDto student,
	AuthorizationCollectorDto collector,
	PermissionType duration,
	OffsetDateTime validFrom,
	OffsetDateTime validUntil,
	LocalTime allowedFromTime,
	List<WeeklyAuthorizationRuleDto> weeklyRules,
	StudentAuthorizationStatus status) {
}
