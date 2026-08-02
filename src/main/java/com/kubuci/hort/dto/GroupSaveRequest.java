package com.kubuci.hort.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupSaveRequest(
	@NotBlank @Size(max = 160) String name
// @NotNull UUID tutorId
) {
}
