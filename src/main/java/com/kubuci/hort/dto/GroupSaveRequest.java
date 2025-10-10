package com.kubuci.hort.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GroupSaveRequest(
	@NotBlank String name
	//@NotNull  Long tutorId
) {}
