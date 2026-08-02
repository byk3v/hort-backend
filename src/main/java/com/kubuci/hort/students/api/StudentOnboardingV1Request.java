package com.kubuci.hort.students.api;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record StudentOnboardingV1Request(
	@NotNull @Valid NewStudentRequest student,
	@NotNull UUID groupId,
	boolean canLeaveAlone,
	@NotEmpty @Valid List<StudentCollectorOnboardingRequest> collectors) {

	@JsonIgnore
	@AssertTrue(message = "exactly one collector must be mainCollector")
	public boolean isExactlyOneMainCollector() {
		return collectors != null && collectors.stream()
			.filter(StudentCollectorOnboardingRequest::mainCollector)
			.count() == 1;
	}
}
