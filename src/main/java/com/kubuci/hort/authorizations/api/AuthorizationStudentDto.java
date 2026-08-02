package com.kubuci.hort.authorizations.api;

import java.util.UUID;

public record AuthorizationStudentDto(
	UUID id,
	String firstName,
	String lastName,
	String groupName) {
}
