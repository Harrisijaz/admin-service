package com.smartInvoice.admin_service.dto;

import com.smartInvoice.admin_service.domain.PlanType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

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

	public record AddNoteRequest(
			@NotBlank @Size(min = 1, max = 1000) String note) {
	}

	public record DismissFlagRequest(
			@Size(max = 300) String note) {
	}
}
