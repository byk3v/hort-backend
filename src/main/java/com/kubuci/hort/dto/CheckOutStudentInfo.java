package com.kubuci.hort.dto;

import java.util.List;
import java.util.UUID;

public record CheckOutStudentInfo(
    UUID studentId,
    String firstName,
    String lastName,
    String groupName,
    boolean canLeaveAloneToday,
    String allowedToLeaveFromTime,
    UUID selfDismissalId,
    boolean checkedOutToday,
    List<CheckOutCollectorInfo> allowedCollectors) {
}
