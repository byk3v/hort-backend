package com.kubuci.hort.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kubuci.hort.enums.PermissionStatus;
import com.kubuci.hort.enums.PermissionType;

public record PickupRightDto(
    UUID id,
    UUID studentId,
    UUID collectorId,
    PermissionType type,
    LocalDateTime validFrom,
    LocalDateTime validUntil,
    PermissionStatus status) {
}
