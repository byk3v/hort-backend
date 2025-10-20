package com.kubuci.hort.dto;

import java.time.LocalDateTime;
import com.kubuci.hort.enums.PermissionType;
import jakarta.validation.constraints.NotNull;

public record PickupRightCreateRequest(
	@NotNull Long studentId,
	@NotNull Long collectorId,
	@NotNull PermissionType type,
	@NotNull LocalDateTime validFrom,
	LocalDateTime validUntil
) {}
