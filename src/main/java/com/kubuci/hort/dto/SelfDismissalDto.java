package com.kubuci.hort.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kubuci.hort.enums.PermissionStatus;

public record SelfDismissalDto(
    UUID id,
    UUID studentId,
    LocalDateTime validFrom,
    LocalDateTime validUntil,
    PermissionStatus status) {
}
