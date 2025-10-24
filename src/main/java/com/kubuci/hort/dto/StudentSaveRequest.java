package com.kubuci.hort.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentSaveRequest(
	@NotNull Long groupId,
	@NotBlank String firstName,
	@NotBlank String lastName,
	String address,
	String phone
) {}