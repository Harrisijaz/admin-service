package com.smartInvoice.admin_service.dto;

import com.smartInvoice.admin_service.domain.PlanType;
import com.smartInvoice.admin_service.domain.PaymentStatus;
import com.smartInvoice.admin_service.domain.SubscriptionStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public final class Requests {
	private Requests() {}

	public record BlockUserRequest(
			@NotBlank @Size(min = 5, max = 500) String reason) {
	}

	public record DeleteUserRequest(
			@NotBlank @Email String confirmationEmail,
			boolean cancelSubscriptionFirst) {
	}

	public record ChangePlanRequest(
			@NotNull PlanType plan,
			@NotBlank @Size(min = 10, max = 500) String reason) {
	}

	public record RefundRequest(
			@NotNull @DecimalMin(value = "0.01") BigDecimal amount,
			@NotBlank @Size(min = 5, max = 1000) String note) {
	}

	public record BillingSyncRequest(
			@NotBlank String userId,
			@NotNull PlanType planType,
			@NotNull SubscriptionStatus subscriptionStatus,
			String subscriptionId,
			Instant startDate,
			Instant renewalDate,
			String gatewayReference,
			PaymentStatus paymentStatus,
			@DecimalMin(value = "0.00") BigDecimal amount,
			Instant paymentCreatedAt,
			Instant retryOrExpiryDate) {
	}

	public record AddNoteRequest(
			@NotBlank @Size(min = 1, max = 1000) String note) {
	}

	public record DismissFlagRequest(
			@Size(max = 300) String note) {
	}
}
