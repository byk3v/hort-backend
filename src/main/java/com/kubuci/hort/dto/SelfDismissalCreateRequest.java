package com.kubuci.hort.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record SelfDismissalCreateRequest(
    @NotNull UUID studentId,
    @NotNull LocalDateTime validFrom,
    LocalDateTime validUntil) {
}
