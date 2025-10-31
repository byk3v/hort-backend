package com.kubuci.hort.models;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.kubuci.hort.enums.PermissionStatus;
import com.kubuci.hort.models.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "self_dismissal", indexes = @Index(name = "idx_self_dismissal_student", columnList = "student_id"))
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SelfDismissal extends BaseEntity {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, foreignKey = @ForeignKey(name = "fk_self_dismissal_student"))
    private Student student;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "allowed_from_time")
    private LocalTime allowedFromTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PermissionStatus status;
}
