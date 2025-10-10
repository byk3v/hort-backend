package com.kubuci.hort.dto;

import java.time.LocalDate;
import java.util.List;

public record StudentDto(
	Long id,
	String firstName,
	String lastName,
	String address,
	String group,                  // group name
	boolean canLeaveAlone,
	LocalDate allowedTimeToLeave,
	List<CollectorDto> collectors
) {

}
