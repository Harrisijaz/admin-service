package com.smartInvoice.admin_service.dto;

import com.smartInvoice.admin_service.domain.*;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class Responses {
	private Responses() {}

	public record MessageResponse(String code, String message) {
	}

	public record UserSummary(
			String id,
			String name,
			String email,
			Instant signupDate,
			PlanType planType,
			AccountStatus accountStatus) {
	}

	public record UserDetail(
			UserSummary profile,
			List<Subscription> subscriptionHistory,
			List<Payment> paymentHistory,
			List<AdminNote> internalNotes,
			Object invoices,
			Object expenses) {
	}

	public record CsvExport(String filename, String contentType, String csv) {
	}

	public record UsersPage(Page<UserSummary> users, String emptyStateMessage) {
	}

	public record DashboardResponse(
			long totalUsers,
			long freeUsers,
			long paidUsers,
			BigDecimal currentMonthRevenue,
			BigDecimal previousMonthRevenue,
			BigDecimal percentageChangeVsPreviousMonth,
			List<SignupPoint> signups,
			long churnCount,
			BigDecimal churnPercentage,
			String zeroStateMessage) {
	}

	public record SignupPoint(LocalDate date, long count) {
	}

	public record AiUsageOverview(
			LocalDate billingCycleStart,
			List<AiUsageRecord> users,
			BigDecimal totalEstimatedCost,
			BigDecimal subscriptionRevenue,
			BigDecimal costToRevenuePercentage) {
	}

	public record PasswordResetResponse(String message, Instant nextAllowedAt) {
	}

	public record SupportRecords(Object invoices, Object expenses) {
	}

	public record ModerationOverview(List<ModerationFlag> openFlags, Map<String, String> guidance) {
	}
}
