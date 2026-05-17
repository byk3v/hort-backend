package com.kubuci.hort.dto;

import java.util.UUID;

public record PersonDto(
    UUID id,
    String firstName,
    String lastName,
    String address,
    String phone) {
}
