package com.kubuci.hort.authorizations.api;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kubuci.hort.collectors.api.CollectorWriteRequest;
import com.kubuci.hort.students.api.CollectorOnboardingSource;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record AuthorizationCollectorRequest(
	@NotNull CollectorOnboardingSource source,
	UUID existingCollectorId,
	@Valid CollectorWriteRequest newCollector) {

	@JsonIgnore
	@AssertTrue(message = "must provide exactly the collector payload required by source")
	public boolean isSourcePayloadValid() {
		return source == CollectorOnboardingSource.EXISTING
			? existingCollectorId != null && newCollector == null
			: source == CollectorOnboardingSource.NEW && existingCollectorId == null && newCollector != null;
	}
}
