package com.kubuci.hort.dto;

import java.util.List;
import java.util.UUID;

public record StudentOnboardingRequest(
    StudentDto student,
    UUID groupId,
    List<CollectorForOnboarding> collectors) {
}
