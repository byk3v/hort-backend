package com.kubuci.hort.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record NewPermissionRequest(
    UUID studentId,
    String kind, // "TAGES" | "DAUER"
    Boolean canLeaveAlone, // true => selfDismissal

    // rango de validez
    LocalDateTime validFrom,
    LocalDateTime validUntil,

    // para Tagesvollmacht canLeaveAlone=true:
    LocalTime allowedFromTime,

    // datos del collector si aplica
    NewPermissionCollectorInlineDto collector,

    // horarios semanales si es DAUER + selfDismissal
    NewPermissionWeeklyAllowedFrom weeklyAllowedFrom) {
}
