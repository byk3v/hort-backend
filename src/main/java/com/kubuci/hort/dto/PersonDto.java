package com.kubuci.hort.dto;

public record PersonDto(
	Long id,
	String firstName,
	String lastName,
	String address,
	String phone
) {}
