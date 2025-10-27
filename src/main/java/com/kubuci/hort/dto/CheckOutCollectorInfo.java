package com.kubuci.hort.dto;

public record CheckOutCollectorInfo(
    Long collectorId,
    String firstName,
    String lastName,
    String phone,
    boolean mainCollector,
    String allowedFromTime,
    Long pickupRightId) {
}
