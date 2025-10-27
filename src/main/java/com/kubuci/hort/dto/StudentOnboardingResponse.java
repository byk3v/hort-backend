package com.kubuci.hort.dto;

import java.util.List;

public record StudentOnboardingResponse(
    Long studentId,
    List<Long> collectorIds,
    List<Long> pickupRightIds) {
}
