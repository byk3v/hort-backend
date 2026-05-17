package com.kubuci.hort.models;

import java.time.LocalDateTime;

import com.kubuci.hort.models.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "student", uniqueConstraints = @UniqueConstraint(name = "uk_student_person", columnNames = "person_id"), indexes = {
        @Index(name = "idx_student_hort", columnList = "hort_id"),
        @Index(name = "idx_student_group", columnList = "group_id") })
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Student extends BaseEntity {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "hort_id", nullable = false, foreignKey = @ForeignKey(name = "fk_student_hort"))
    private Hort hort;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false, foreignKey = @ForeignKey(name = "fk_student_person"))
    private Person person;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false, foreignKey = @ForeignKey(name = "fk_student_group"))
    private HortGroup group;

    @Column(name = "allowed_time_to_leave")
    private LocalDateTime allowedTimeToLeave;

    @Column(name = "can_leave_alone", nullable = false)
    private boolean canLeaveAlone;
}
