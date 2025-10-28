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
	@JoinColumn(name = "pickup_right_id", foreignKey = @ForeignKey(name="fk_checkout_pickup_right"))
	private PickupRight pickupRight; // si fue COLLECTOR

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "self_dismissal_id", foreignKey = @ForeignKey(name="fk_checkout_self_perm"))
	private SelfDismissal selfDismissal; // si fue STUDENT

	@Column(name = "comment", length = 500)
	private String comment;

	@Column(name = "recorded_by_user_id")//hasta que esten los usuarios
	private String recordedByUserId;

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

	public CollectorType getCollectorType() {
		return collectorType;
	}

	public void setCollectorType(CollectorType collectorType) {
		this.collectorType = collectorType;
	}

	public Collector getCollector() {
		return collector;
	}

	public void setCollector(Collector collector) {
		this.collector = collector;
	}

	public LocalDateTime getOccurredAt() {
		return occurredAt;
	}

	public void setOccurredAt(LocalDateTime occurredAt) {
		this.occurredAt = occurredAt;
	}

	public PickupRight getPickupRight() {
		return pickupRight;
	}

	public void setPickupRight(PickupRight pickupRight) {
		this.pickupRight = pickupRight;
	}

	public SelfDismissal getSelfDismissal() {
		return selfDismissal;
	}

	public void setSelfDismissal(SelfDismissal selfDismissal) {
		this.selfDismissal = selfDismissal;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getRecordedByUserId() {
		return recordedByUserId;
	}

	public void setRecordedByUserId(String recordedByUserId) {
		this.recordedByUserId = recordedByUserId;
	}
}
