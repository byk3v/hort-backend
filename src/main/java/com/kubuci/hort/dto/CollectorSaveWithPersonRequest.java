package com.kubuci.hort.dto;

import jakarta.validation.constraints.NotBlank;

public record CollectorSaveWithPersonRequest(
	@NotBlank String firstName,
	@NotBlank  String lastName,
	String address,
	String phone,
	@NotBlank String collectorType
) {

}
