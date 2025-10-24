package com.kubuci.hort.dto;

import java.util.List;

public record StudentOnboardingRequest(
	StudentDto student,
	Long groupId,
	List<CollectorForOnboarding> collectors
) {}
