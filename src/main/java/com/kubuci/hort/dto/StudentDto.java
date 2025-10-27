package com.kubuci.hort.dto;

import java.util.List;

public record StudentDto(
    Long id,
    String firstName,
    String lastName,
    String address,
    String group,
    List<CollectorDto> collectors) {

}
