package com.kubuci.hort.models;

import java.time.LocalDateTime;
import java.time.LocalTime;
import com.kubuci.hort.enums.PermissionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "self_dismissal")
public class SelfDismissal {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public LocalDateTime getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDateTime validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDateTime getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(LocalDateTime validUntil) {
		this.validUntil = validUntil;
	}

	public LocalTime getAllowedFromTime() {
		return allowedFromTime;
	}

	public void setAllowedFromTime(LocalTime allowedFromTime) {
		this.allowedFromTime = allowedFromTime;
	}

	public PermissionStatus getStatus() {
		return status;
	}

	public void setStatus(PermissionStatus status) {
		this.status = status;
	}
}
