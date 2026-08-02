package com.kubuci.hort.models;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.kubuci.hort.models.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "self_dismissal_weekly_rule", uniqueConstraints = @UniqueConstraint(
	name = "uk_self_dismissal_weekday", columnNames = { "self_dismissal_id", "day_of_week" }))
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SelfDismissalWeeklyRule extends BaseEntity {

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "hort_id", nullable = false)
	private Hort hort;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "self_dismissal_id", nullable = false)
	private SelfDismissal selfDismissal;

	@Enumerated(EnumType.STRING)
	@Column(name = "day_of_week", nullable = false, length = 9)
	private DayOfWeek dayOfWeek;

	@Column(name = "allowed_from_time", nullable = false)
	private LocalTime allowedFromTime;
}
