package com.kubuci.hort.dto;

import jakarta.validation.constraints.NotNull;

public record CheckOutCreateRequest(
    @NotNull Long studentId,
    Long collectorId, // null si es self-dismissal
    Long pickupRightId, // null si es self-dismissal
    Boolean selfDismissal, // true si se va solo, false si lo recoge alguien
    String comment
// @NotBlank String recordedByUserId
) {
}
