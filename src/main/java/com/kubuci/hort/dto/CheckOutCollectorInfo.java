package com.kubuci.hort.dto;

import java.util.UUID;

public record CheckOutCollectorInfo(
    UUID collectorId,
    String firstName,
    String lastName,
    String phone,
    boolean mainCollector,
    String allowedFromTime,
    UUID pickupRightId) {
}
