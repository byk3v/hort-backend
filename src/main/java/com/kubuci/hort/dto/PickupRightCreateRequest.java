package com.kubuci.hort.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kubuci.hort.enums.PermissionType;

import jakarta.validation.constraints.NotNull;

public record PickupRightCreateRequest(
    @NotNull UUID studentId,
    @NotNull UUID collectorId,
    @NotNull PermissionType type,
    @NotNull LocalDateTime validFrom,
    LocalDateTime validUntil) {
}
