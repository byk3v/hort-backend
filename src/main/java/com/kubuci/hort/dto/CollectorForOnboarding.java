package com.kubuci.hort.dto;

import java.time.OffsetDateTime;

import com.kubuci.hort.enums.CollectorType;
import com.kubuci.hort.enums.PermissionType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CollectorForOnboarding(
	@NotBlank String firstName,
	@NotBlank String lastName,
	String address,
	String phone,
	OffsetDateTime validFrom,
	OffsetDateTime validUntil,
	@NotNull CollectorType type, // "Aqui tengo adulto o alumno", hay que adicionar un boolean MainCollector
	@NotNull PermissionType permissionType,
	boolean mainCollector) {
}
