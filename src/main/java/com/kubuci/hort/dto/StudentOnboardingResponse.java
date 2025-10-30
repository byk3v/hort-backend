package com.kubuci.hort.dto;

import java.util.List;
import java.util.UUID;

public record StudentOnboardingResponse(
    UUID studentId,
    List<UUID> collectorIds,
    List<UUID> pickupRightIds) {
}
