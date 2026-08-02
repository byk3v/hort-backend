package com.kubuci.hort.attendance.api;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AttendanceCheckOutRequest(
	@NotNull UUID attendanceId,
	@NotNull CheckoutMethod method,
	UUID collectorId,
	UUID pickupRightId,
	UUID selfDismissalId,
	@Size(max = 500) String comment) {

	@JsonIgnore
	@AssertTrue(message = "checkout payload does not match method")
	public boolean isMethodPayloadValid() {
		if (method == null) return true;
		return method == CheckoutMethod.PICKUP
			? collectorId != null && pickupRightId != null && selfDismissalId == null
			: collectorId == null && pickupRightId == null && selfDismissalId != null;
	}
}
