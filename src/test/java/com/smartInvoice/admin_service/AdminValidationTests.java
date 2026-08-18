package com.smartInvoice.admin_service;

import com.smartInvoice.admin_service.service.AdminValidation;
import com.smartInvoice.admin_service.web.ApiException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminValidationTests {
	@Test
	void searchQueryMustBeBetweenTwoAndOneHundredCharactersWhenProvided() {
		assertDoesNotThrow(() -> AdminValidation.validateSearch(null));
		assertDoesNotThrow(() -> AdminValidation.validateSearch("ha"));
		assertThrows(ApiException.class, () -> AdminValidation.validateSearch("h"));
		assertThrows(ApiException.class, () -> AdminValidation.validateSearch("x".repeat(101)));
	}

	@Test
	void instantRangeRejectsStartAfterEndAndTooLargeRanges() {
		Instant start = Instant.parse("2026-01-01T00:00:00Z");
		Instant end = Instant.parse("2026-01-02T00:00:00Z");
		assertDoesNotThrow(() -> AdminValidation.validateInstantRange(start, end, Duration.ofDays(365 * 2L)));
		assertThrows(ApiException.class, () -> AdminValidation.validateInstantRange(end, start, Duration.ofDays(365 * 2L)));
		assertThrows(ApiException.class, () -> AdminValidation.validateInstantRange(start, start.plus(Duration.ofDays(731)), Duration.ofDays(365 * 2L)));
	}

	@Test
	void analyticsDateRangeRejectsFutureAndMoreThanTwelveMonthsLookback() {
		LocalDate today = LocalDate.of(2026, 8, 18);
		assertDoesNotThrow(() -> AdminValidation.validateNoFuture(today.minusDays(7), today, today));
		assertThrows(ApiException.class, () -> AdminValidation.validateNoFuture(today, today.plusDays(1), today));
		assertThrows(ApiException.class, () -> AdminValidation.validateNoFuture(today.minusMonths(13), today, today));
	}
}
