package com.smartInvoice.admin_service.service;

import com.smartInvoice.admin_service.domain.*;
import com.smartInvoice.admin_service.dto.Responses.*;
import com.smartInvoice.admin_service.repo.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsAdminService {
	private final UserRepository users;
	private final SubscriptionRepository subscriptions;
	private final PaymentRepository payments;
	private final AiUsageRecordRepository aiUsage;
	private final Clock clock;

	public AnalyticsAdminService(UserRepository users, SubscriptionRepository subscriptions, PaymentRepository payments,
			AiUsageRecordRepository aiUsage, Clock clock) {
		this.users = users;
		this.subscriptions = subscriptions;
		this.payments = payments;
		this.aiUsage = aiUsage;
		this.clock = clock;
	}

	public DashboardResponse dashboard(LocalDate startDate, LocalDate endDate) {
		LocalDate today = LocalDate.now(clock);
		if (startDate == null) startDate = today.minusDays(30);
		if (endDate == null) endDate = today;
		AdminValidation.validateNoFuture(startDate, endDate, today);
		YearMonth month = YearMonth.from(today);
		Instant currentStart = month.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
		Instant currentEnd = month.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
		Instant previousStart = month.minusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();

		BigDecimal currentRevenue = payments.recognizedRevenue(currentStart, currentEnd);
		BigDecimal previousRevenue = payments.recognizedRevenue(previousStart, currentStart);
		long paid = subscriptions.countByPlanTypeAndStatusIn(PlanType.PAID, List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE));
		long total = users.countByRoleAndStatusNot(UserRole.USER, AccountStatus.DELETED);
		long churn = subscriptions.countByPlanTypeAndCancelledAtBetween(PlanType.PAID,
				startDate.atStartOfDay(ZoneOffset.UTC).toInstant(), endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());
		return new DashboardResponse(total, Math.max(total - paid, 0), paid, currentRevenue, previousRevenue,
				percentageChange(currentRevenue, previousRevenue), signups(startDate, endDate), churn,
				paid == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(churn * 100.0 / paid).setScale(2, RoundingMode.HALF_UP),
				total == 0 && currentRevenue.compareTo(BigDecimal.ZERO) == 0 ? "No revenue yet - check back once you have your first subscriber" : null);
	}

	public AiUsageOverview aiUsage(LocalDate cycleStart) {
		if (cycleStart == null) {
			LocalDate today = LocalDate.now(clock);
			cycleStart = LocalDate.of(today.getYear(), today.getMonth(), 1);
		}
		YearMonth month = YearMonth.from(cycleStart);
		BigDecimal revenue = payments.recognizedRevenue(month.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant(),
				month.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant());
		BigDecimal cost = aiUsage.totalCostForCycle(cycleStart);
		return new AiUsageOverview(cycleStart, aiUsage.findByBillingCycleStartOrderByCountDesc(cycleStart), cost, revenue, percentage(cost, revenue));
	}

	private List<SignupPoint> signups(LocalDate start, LocalDate end) {
		List<SignupPoint> points = new ArrayList<>();
		for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
			Instant from = date.atStartOfDay(ZoneOffset.UTC).toInstant();
			Instant to = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
			points.add(new SignupPoint(date, users.countByRoleAndStatusNotAndCreatedAtBetween(UserRole.USER, AccountStatus.DELETED, from, to)));
		}
		return points;
	}

	private BigDecimal percentageChange(BigDecimal current, BigDecimal previous) {
		if (previous.compareTo(BigDecimal.ZERO) == 0) return current.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100);
		return current.subtract(previous).multiply(BigDecimal.valueOf(100)).divide(previous, 2, RoundingMode.HALF_UP);
	}

	private BigDecimal percentage(BigDecimal part, BigDecimal total) {
		if (total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
		return part.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
	}
}
