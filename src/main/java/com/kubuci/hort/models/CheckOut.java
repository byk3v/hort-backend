package com.kubuci.hort.models;

import java.time.LocalDate;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "check_out", indexes = {
	@Index(name = "idx_checkout_student_date", columnList = "id_student, date_time")
})
public class CheckOut {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_student", nullable = false, foreignKey = @ForeignKey(name = "fk_checkout_student"))
	private Student student;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_collector", nullable = false, foreignKey = @ForeignKey(name = "fk_checkout_collector"))
	private Collector collector;

	@Column(name = "date_time", nullable = false)
	@CreationTimestamp
	private LocalDate dateTime;

	@Column(name = "comment", length = 500)
	private String comment;

	@Column(name = "user_id", nullable = false)
	private String userId;
}
