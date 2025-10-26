package com.kubuci.hort.models;

import java.time.LocalDateTime;
import java.time.LocalTime;
import com.kubuci.hort.enums.PermissionStatus;
import com.kubuci.hort.enums.PermissionType;
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

@Entity
@Table(name = "pickup_right")
public class PickupRight {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

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

	public boolean isMainCollector() {
		return mainCollector;
	}

	public void setMainCollector(boolean mainCollector) {
		this.mainCollector = mainCollector;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PermissionType getType() {
		return type;
	}

	public void setType(PermissionType type) {
		this.type = type;
	}

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public Collector getCollector() {
		return collector;
	}

	public void setCollector(Collector collector) {
		this.collector = collector;
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