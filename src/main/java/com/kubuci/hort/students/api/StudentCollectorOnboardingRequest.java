package com.kubuci.hort.students.api;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kubuci.hort.enums.PermissionType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record StudentCollectorOnboardingRequest(
	@NotNull CollectorOnboardingSource source,
	UUID existingCollectorId,
	@Valid NewCollectorRequest newCollector,
	@NotNull PermissionType permissionType,
	LocalDateTime validFrom,
	LocalDateTime validUntil,
	boolean mainCollector) {

	@JsonIgnore
	@AssertTrue(message = "must provide exactly the payload required by source")
	public boolean isSourcePayloadValid() {
		return source == CollectorOnboardingSource.EXISTING
			? existingCollectorId != null && newCollector == null
			: source == CollectorOnboardingSource.NEW && existingCollectorId == null && newCollector != null;
	}

	@JsonIgnore
	@AssertTrue(message = "validUntil must not be before validFrom")
	public boolean isDateRangeValid() {
		return validFrom == null || validUntil == null || !validUntil.isBefore(validFrom);
	}
}
