package com.kubuci.hort.models;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.kubuci.hort.enums.CollectorType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "check_out")
public class CheckOut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, foreignKey = @ForeignKey(name = "fk_checkout_student"))
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "collector_type", nullable = false, length = 16)
    private CollectorType collectorType;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "collector_id", foreignKey = @ForeignKey(name = "fk_checkout_collector"))
    private Collector collector;

    @Column(name = "occurred_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime occurredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_right_id", foreignKey = @ForeignKey(name = "fk_checkout_pickup_right"))
    private PickupRight pickupRight; // si fue COLLECTOR

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "self_dismissal_id", foreignKey = @ForeignKey(name = "fk_checkout_self_perm"))
    private SelfDismissal selfDismissal; // si fue STUDENT

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "recorded_by_user_id", nullable = false) // hasta que esten los usuarios
    private String recordedByUserId;

}
