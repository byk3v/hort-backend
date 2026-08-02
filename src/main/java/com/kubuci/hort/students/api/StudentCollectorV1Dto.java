package com.kubuci.hort.students.api;

import java.util.UUID;

import com.kubuci.hort.enums.CollectorType;

public record StudentCollectorV1Dto(
	UUID id,
	String firstName,
	String lastName,
	String address,
	String phone,
	CollectorType collectorType,
	UUID pickupRightId,
	boolean mainCollector) {
}
