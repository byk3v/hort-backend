package com.kubuci.hort.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CollectorSaveRequest(
    @NotNull UUID personId,
    @NotBlank String collectorType) {

}
