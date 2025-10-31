package com.kubuci.hort.dto;

import java.util.List;

public record CheckOutSearchResponse(
    List<CheckOutStudentInfo> students) {
}
