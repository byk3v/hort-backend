package com.kubuci.hort.dto;

import java.util.UUID;

public record CollectorDto(
    UUID id,
    String firstName,
    String lastName,
    String address,
    String phone,
    String collectorType) {
}
