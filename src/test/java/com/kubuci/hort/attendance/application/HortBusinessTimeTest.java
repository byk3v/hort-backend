package com.kubuci.hort.attendance.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class HortBusinessTimeTest {

	@Test
	void usesBerlinBusinessDateWhenUtcIsStillPreviousDay() {
		var time = new HortBusinessTime(Clock.fixed(Instant.parse("2026-08-02T22:30:00Z"), ZoneOffset.UTC));

		assertThat(time.businessDate()).isEqualTo(LocalDate.of(2026, 8, 3));
		assertThat(time.now().getOffset()).isEqualTo(ZoneOffset.UTC);
	}

	@Test
	void respectsBerlinDaylightSavingOffset() {
		var summer = new HortBusinessTime(Clock.fixed(Instant.parse("2026-06-01T22:30:00Z"), ZoneOffset.UTC));
		var winter = new HortBusinessTime(Clock.fixed(Instant.parse("2026-01-01T23:30:00Z"), ZoneOffset.UTC));

		assertThat(summer.businessDate()).isEqualTo(LocalDate.of(2026, 6, 2));
		assertThat(winter.businessDate()).isEqualTo(LocalDate.of(2026, 1, 2));
	}
}
