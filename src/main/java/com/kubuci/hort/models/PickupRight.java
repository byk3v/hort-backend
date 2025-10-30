package com.kubuci.hort.models;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.kubuci.hort.enums.PermissionStatus;
import com.kubuci.hort.enums.PermissionType;
import com.kubuci.hort.models.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "pickup_right")
public class PickupRight extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private PermissionType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, foreignKey = @ForeignKey(name = "fk_permission_student"))
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collector_id", nullable = false, foreignKey = @ForeignKey(name = "fk_permission_collector"))
    private Collector collector;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until", nullable = true)
    private LocalDateTime validUntil;

    @Column(name = "allowed_from_time")
    private LocalTime allowedFromTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionStatus status;

    @Column(name = "main_collector", nullable = false)
    private boolean mainCollector;

}