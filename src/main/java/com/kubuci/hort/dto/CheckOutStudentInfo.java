package com.kubuci.hort.dto;

import java.util.List;

public record CheckOutStudentInfo(
	Long studentId,
	String firstName,
	String lastName,
	String groupName,
	boolean canLeaveAloneToday,
	String allowedToLeaveFromTime,
	Long selfDismissalId,
	List<CheckOutCollectorInfo> allowedCollectors
) {}
