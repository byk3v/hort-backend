package com.kubuci.hort.dto;

public record NewPermissionWeeklyAllowedFrom(
    String monday, // "15:30" or null
    String tuesday,
    String wednesday,
    String thursday,
    String friday) {
}
