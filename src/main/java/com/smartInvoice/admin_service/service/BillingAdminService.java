package com.smartInvoice.admin_service.service;

import com.smartInvoice.admin_service.domain.*;
import com.smartInvoice.admin_service.dto.Requests;
import com.smartInvoice.admin_service.dto.Responses.MessageResponse;
import com.smartInvoice.admin_service.repo.PaymentRepository;
import com.smartInvoice.admin_service.repo.SubscriptionRepository;
import com.smartInvoice.admin_service.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BillingAdminService {
	private final SubscriptionRepository subscriptions;
	private final PaymentRepository payments;
	private final AuditService audit;
	private final Clock clock;

	public BillingAdminService(SubscriptionRepository subscriptions, PaymentRepository payments, AuditService audit, Clock clock) {
		this.subscriptions = subscriptions;
		this.payments = payments;
		this.audit = audit;
		this.clock = clock;
	}

	public List<Subscription> history(String userId) {
		return subscriptions.findByUserIdOrderByStartDateDesc(userId);
	}

	public List<Payment> failedOrPendingPayments() {
		return payments.findByStatusInOrderByCreatedAtDesc(List.of(PaymentStatus.FAILED, PaymentStatus.PENDING));
	}

	@Transactional
	public Subscription changePlan(String userId, Requests.ChangePlanRequest request, AdminPrincipal admin) {
		Subscription current = subscriptions.findFirstByUserIdOrderByStartDateDesc(userId).orElseGet(() -> {
			Subscription s = new Subscription();
			s.setId("sub_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
			s.setUserId(userId);
			s.setStartDate(Instant.now(clock));
			return s;
		});
		if (current.getPlanType() == request.plan()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "PLAN_NOOP", "User is already on the " + request.plan() + " plan.");
		}
		current.setPlanType(request.plan());
		current.setStatus(request.plan() == PlanType.PAID ? SubscriptionStatus.ACTIVE : SubscriptionStatus.CANCELLED);
		current.setManualOverride(true);
		current.setManualOverrideReason(request.reason().trim());
		current.setManualOverrideAt(Instant.now(clock));
		if (request.plan() == PlanType.FREE) {
			current.setCancelledAt(Instant.now(clock));
			current.setRenewalDate(null);
		}
		Subscription saved = subscriptions.save(current);
		audit.log(admin, AdminActionType.CHANGE_PLAN, userId, "Immediate Admin Override plan change to " + request.plan() + ". Reason: " + request.reason().trim());
		return saved;
	}

	@Transactional
	public Payment refund(String paymentId, Requests.RefundRequest request, AdminPrincipal admin) {
		Payment payment = payments.findById(paymentId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", "Payment not found."));
		if (request.amount().compareTo(payment.getAmount()) > 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "REFUND_TOO_LARGE", "Refund amount cannot exceed the original payment amount.");
		}
		payment.setStatus(PaymentStatus.REFUNDED);
		payment.setRefundedAmount(request.amount());
		payment.setRefundNote(request.note().trim());
		payment.setRefundedAt(Instant.now(clock));
		audit.log(admin, AdminActionType.LOG_REFUND, payment.getUserId(), "Logged refund for payment " + paymentId + ".");
		return payment;
	}

	@Transactional
	public void applyGatewayPaidEvent(String userId, String gatewayReference) {
		subscriptions.findFirstByUserIdOrderByStartDateDesc(userId).ifPresentOrElse(s -> {
			if (s.isManualOverride() && s.getPlanType() == PlanType.FREE) s.setSuperseded(true);
			s.setPlanType(PlanType.PAID);
			s.setStatus(SubscriptionStatus.ACTIVE);
			s.setGatewayReference(gatewayReference);
		}, () -> {
			Subscription s = new Subscription();
			s.setId("sub_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
			s.setUserId(userId);
			s.setPlanType(PlanType.PAID);
			s.setStatus(SubscriptionStatus.ACTIVE);
			s.setStartDate(Instant.now(clock));
			s.setGatewayReference(gatewayReference);
			subscriptions.save(s);
		});
	}
}
