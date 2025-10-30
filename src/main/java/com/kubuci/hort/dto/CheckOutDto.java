package com.kubuci.hort.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kubuci.hort.enums.CollectorType;

public record CheckOutDto(
    UUID id,
    UUID studentId,
    CollectorType collectorType,
    UUID collectorId,
    LocalDateTime occurredAt,
    UUID pickupRightId,
    UUID selfDismissalId,
    String comment,
    String recordedByUserId) {
}
