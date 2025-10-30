package com.kubuci.hort.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CheckOutCreateRequest(
    @NotNull UUID studentId,
    UUID collectorId, // null si es self-dismissal
    UUID pickupRightId, // null si es self-dismissal
    Boolean selfDismissal, // true si se va solo, false si lo recoge alguien
    String comment
// @NotBlank String recordedByUserId
) {
}
