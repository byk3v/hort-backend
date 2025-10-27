package com.kubuci.hort.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentSaveRequest(
    @NotNull Long groupId,
    @NotBlank String firstName,
    @NotBlank String lastName,
    String address,
    String phone) {
}