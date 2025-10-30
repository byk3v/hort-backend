package com.kubuci.hort.models;

import static jakarta.persistence.FetchType.LAZY;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.kubuci.hort.enums.PermissionStatus;
import com.kubuci.hort.models.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "self_dismissal")
public class SelfDismissal extends BaseEntity {

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "allowed_from_time")
    private LocalTime allowedFromTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionStatus status;

}
