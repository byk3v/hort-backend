package com.kubuci.hort.authorizations.api;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public record WeeklyAuthorizationRuleDto(
	@NotNull DayOfWeek dayOfWeek,
	@NotNull LocalTime allowedFromTime) {
}
