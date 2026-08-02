package com.kubuci.hort.students.api;

import java.util.List;
import java.util.UUID;

public record StudentV1Dto(
	UUID id,
	String firstName,
	String lastName,
	String address,
	String phone,
	StudentGroupV1Dto group,
	List<StudentCollectorV1Dto> collectors) {
}
