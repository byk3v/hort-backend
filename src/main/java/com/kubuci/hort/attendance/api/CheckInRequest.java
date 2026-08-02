package com.kubuci.hort.attendance.api;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CheckInRequest(@NotNull UUID studentId, @Size(max = 500) String comment) {
}
