package com.kubuci.hort.dto;

public record PermissionViewDto(
	Long permissionId,          // id del permiso (PickupRight.id o SelfDismissal.id)
	String permissionKind,      // "COLLECTOR" o "SELF_DISMISSAL"

	Long studentId,
	String studentFirstName,
	String studentLastName,
	String studentGroupName,

	Long collectorId,           // null si es self dismissal
	String collectorFirstName,  // null si es self dismissal
	String collectorLastName,   // null si es self dismissal
	String collectorPhone,      // null si es self dismissal

	String validFrom,           // "2025-01-20T14:30:00"
	String validUntil,          // puede ser null => sin fin
	String allowedFromTime,     // "15:30" o null
	String status               // ej. "ACTIVE", "INACTIVE", ...
) {}
