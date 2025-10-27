package com.kubuci.hort.dto;

import jakarta.validation.constraints.NotBlank;

public record GroupSaveRequest(
    @NotBlank String name
// @NotNull Long tutorId
) {
}
