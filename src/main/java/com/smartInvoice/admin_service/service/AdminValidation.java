package com.smartInvoice.admin_service.service;

import com.smartInvoice.admin_service.web.ApiException;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

public final class AdminValidation {
	private AdminValidation() {}

	public static String trimmed(String value) {
		return value == null ? null : value.trim();
	}

	public static void validateSearch(String search) {
		if (search == null || search.isBlank()) return;
		int length = search.trim().length();
		if (length < 2 || length > 100) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SEARCH_QUERY", "Search query must be 2-100 characters.");
		}
	}

	public static void validateInstantRange(Instant start, Instant end, Duration maxRange) {
		if (start == null || end == null) return;
		if (start.isAfter(end)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE", "startDate must be before or equal to endDate.");
		}
		if (Duration.between(start, end).compareTo(maxRange) > 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "DATE_RANGE_TOO_LARGE", "Date range exceeds the maximum allowed range.");
		}
	}

	public static void validateNoFuture(LocalDate start, LocalDate end, LocalDate today) {
		if (start == null || end == null) return;
		if (start.isAfter(end)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE", "startDate must be before or equal to endDate.");
		}
		if (start.isAfter(today) || end.isAfter(today)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "FUTURE_DATE_RANGE", "Date range cannot be in the future.");
		}
		if (start.isBefore(today.minusMonths(12))) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "LOOKBACK_TOO_LARGE", "Maximum lookback is 12 months.");
		}
	}
}
