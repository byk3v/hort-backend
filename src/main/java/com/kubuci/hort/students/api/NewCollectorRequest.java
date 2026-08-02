package com.kubuci.hort.students.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewCollectorRequest(
	@NotBlank @Size(max = 120) String firstName,
	@NotBlank @Size(max = 120) String lastName,
	@Size(max = 250) String address,
	@Size(max = 40) String phone) {
}
