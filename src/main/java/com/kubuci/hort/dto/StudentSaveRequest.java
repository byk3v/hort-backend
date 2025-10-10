package com.kubuci.hort.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentSaveRequest(
	@NotNull Long groupId,
	boolean canLeaveAlone,
	LocalDate allowedTimeToLeave,
	@NotBlank String firstName,
	@NotBlank String lastName,
	String address,
	String phone
) {

}
