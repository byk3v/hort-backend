package com.kubuci.hort.collectors.api;

import java.util.UUID;

import com.kubuci.hort.enums.CollectorType;

public record CollectorV1Dto(
	UUID id,
	String firstName,
	String lastName,
	String address,
	String phone,
	CollectorType collectorType) {
}
