package com.kubuci.hort.dto;

import java.util.List;
import java.util.UUID;

public record StudentDto(
    UUID id,
    String firstName,
    String lastName,
    String address,
    String group,
    List<CollectorDto> collectors) {

}
