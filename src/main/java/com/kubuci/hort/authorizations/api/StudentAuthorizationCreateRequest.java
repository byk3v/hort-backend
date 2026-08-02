package com.kubuci.hort.authorizations.api;

import java.time.OffsetDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kubuci.hort.enums.PermissionType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record StudentAuthorizationCreateRequest(
	@NotNull StudentAuthorizationKind kind,
	@NotNull UUID studentId,
	@NotNull PermissionType duration,
	@NotNull OffsetDateTime validFrom,
	OffsetDateTime validUntil,
	LocalTime allowedFromTime,
	@Valid AuthorizationCollectorRequest collector,
	@Valid List<WeeklyAuthorizationRuleDto> weeklyRules) {

	@JsonIgnore
	@AssertTrue(message = "validUntil must not be before validFrom")
	public boolean isDateRangeValid() {
		return validFrom == null || validUntil == null || !validUntil.isBefore(validFrom);
	}

	@JsonIgnore
	@AssertTrue(message = "payload does not match authorization kind and duration")
	public boolean isKindPayloadValid() {
		List<WeeklyAuthorizationRuleDto> rules = weeklyRules == null ? List.of() : weeklyRules;
		if (kind == StudentAuthorizationKind.PICKUP_RIGHT) {
			return collector != null && rules.isEmpty();
		}
		if (kind != StudentAuthorizationKind.SELF_DISMISSAL || collector != null) return false;
		return duration == PermissionType.DAILY
			? allowedFromTime != null && rules.isEmpty()
			: duration == PermissionType.PERMANENT && allowedFromTime == null && !rules.isEmpty();
	}

	@JsonIgnore
	@AssertTrue(message = "weekly rule weekdays must be unique and limited to Monday through Friday")
	public boolean isWeeklyRulesValid() {
		if (weeklyRules == null) return true;
		var weekdays = new HashSet<>();
		return weeklyRules.stream().allMatch(rule -> rule != null
			&& rule.dayOfWeek() != null
			&& rule.dayOfWeek().getValue() <= 5
			&& weekdays.add(rule.dayOfWeek()));
	}
}
