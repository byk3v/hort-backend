package com.kubuci.hort.dto;

import jakarta.validation.constraints.NotBlank;

public record GroupUpdateRequest(
    @NotBlank String name
// @NotNull UUID tutorId
) {
}
