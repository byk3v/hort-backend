package com.kubuci.hort.attendance.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

@Component
public class HortBusinessTime {
	public static final ZoneId ZONE = ZoneId.of("Europe/Berlin");

	private final Clock clock;

	public HortBusinessTime(Clock clock) {
		this.clock = clock;
	}

	public OffsetDateTime now() {
		return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
	}

	public LocalDate businessDate() {
		return LocalDate.now(clock.withZone(ZONE));
	}

	public LocalTime businessTime() {
		return LocalTime.now(clock.withZone(ZONE));
	}
}
