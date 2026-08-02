package com.kubuci.hort.authorizations.api;

import java.util.UUID;

public record AuthorizationCollectorDto(
	UUID id,
	String firstName,
	String lastName,
	String phone) {
}
