package com.kubuci.hort.models;

import java.time.LocalDateTime;

import com.kubuci.hort.enums.CollectorType;
import com.kubuci.hort.models.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "check_out", indexes = {
        @Index(name = "idx_checkout_student_date", columnList = "student_id, occurred_at"),
        @Index(name = "idx_checkout_hort", columnList = "hort_id") })
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class CheckOut extends BaseEntity {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "hort_id", nullable = false, foreignKey = @ForeignKey(name = "fk_check_out_hort"))
    private Hort hort;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, foreignKey = @ForeignKey(name = "fk_check_out_student"))
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "collector_type", nullable = false)
    private CollectorType collectorType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collector_id", foreignKey = @ForeignKey(name = "fk_check_out_collector"))
    private Collector collector; // null si self-dismissal

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_right_id", foreignKey = @ForeignKey(name = "fk_check_out_pickup_right"))
    private PickupRight pickupRight; // null si self-dismissal

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "self_dismissal_id", foreignKey = @ForeignKey(name = "fk_check_out_self_dismissal"))
    private SelfDismissal selfDismissal; // null si lo recoge adulto

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "recorded_by_user_id", nullable = false, length = 255)
    private String recordedByUserId; // sub (Keycloak)
}
