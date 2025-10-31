package com.kubuci.hort.models;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.kubuci.hort.enums.PermissionStatus;
import com.kubuci.hort.enums.PermissionType;
import com.kubuci.hort.models.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pickup_right", indexes = { @Index(name = "idx_pickup_right_hort", columnList = "hort_id"),
        @Index(name = "idx_pickup_right_student", columnList = "student_id") })
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PickupRight extends BaseEntity {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "hort_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pickup_right_hort"))
    private Hort hort;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pickup_right_student"))
    private Student student;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "collector_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pickup_right_collector"))
    private Collector collector;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PermissionType type;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "allowed_from_time")
    private LocalTime allowedFromTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PermissionStatus status;

    @Column(name = "main_collector", nullable = false)
    private boolean mainCollector;
}