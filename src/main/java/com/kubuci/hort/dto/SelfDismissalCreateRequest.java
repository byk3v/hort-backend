package com.kubuci.hort.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record SelfDismissalCreateRequest(
    @NotNull Long studentId,
    @NotNull LocalDateTime validFrom,
    LocalDateTime validUntil) {
}
