package com.kubuci.hort.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CollectorSaveRequest(
	@NotNull Long personId,
	@NotBlank String collectorType
) {

}
