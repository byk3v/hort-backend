package com.kubuci.hort.models;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.kubuci.hort.models.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "attendance_session", uniqueConstraints = @UniqueConstraint(
	name = "uk_attendance_student_day", columnNames = { "hort_id", "student_id", "attendance_date" }))
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class AttendanceSession extends BaseEntity {

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "hort_id", nullable = false)
	private Hort hort;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id", nullable = false)
	private Student student;

	@Column(name = "attendance_date", nullable = false)
	private LocalDate attendanceDate;

	@Column(name = "checked_in_at", nullable = false)
	private OffsetDateTime checkedInAt;

	@Column(name = "checked_in_by_user_id", nullable = false, length = 255)
	private String checkedInByUserId;

	@Column(name = "check_in_comment", length = 500)
	private String checkInComment;

	@Column(name = "checked_out_at")
	private OffsetDateTime checkedOutAt;

	@Column(name = "checked_out_by_user_id", length = 255)
	private String checkedOutByUserId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "check_out_id", unique = true)
	private CheckOut checkOut;
}
