package com.smartInvoice.admin_service.service;

import com.smartInvoice.admin_service.config.AdminProperties;
import com.smartInvoice.admin_service.domain.*;
import com.smartInvoice.admin_service.dto.Requests;
import com.smartInvoice.admin_service.repo.PaymentRepository;
import com.smartInvoice.admin_service.repo.SubscriptionRepository;
import com.smartInvoice.admin_service.repo.UserRepository;
import com.smartInvoice.admin_service.dto.Responses.BillingUser;
import com.smartInvoice.admin_service.dto.Responses.BillingOverview;
import com.smartInvoice.admin_service.dto.Responses.PaymentAdminRow;
import com.smartInvoice.admin_service.dto.Responses.SubscriptionAdminRow;
import com.smartInvoice.admin_service.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class BillingAdminService {
	private final SubscriptionRepository subscriptions;
	private final PaymentRepository payments;
	private final UserRepository users;
	private final AdminProperties properties;
	private final IntegrationClient integrations;
	private final AuditService audit;
	private final Clock clock;

	public BillingAdminService(SubscriptionRepository subscriptions, PaymentRepository payments, UserRepository users,
			AdminProperties properties, IntegrationClient integrations, AuditService audit, Clock clock) {
		this.subscriptions = subscriptions;
		this.payments = payments;
		this.users = users;
		this.properties = properties;
		this.integrations = integrations;
		this.audit = audit;
		this.clock = clock;
	}

	public BillingOverview overview() {
		List<Subscription> subscriptionRows = subscriptions();
		List<Payment> paymentRows = failedOrPendingPayments();
		Map<String, BillingUser> userLookup = usersById(
				java.util.stream.Stream.concat(
								subscriptionRows.stream().map(Subscription::getUserId),
								paymentRows.stream().map(Payment::getUserId))
						.filter(Objects::nonNull)
						.collect(Collectors.toSet()));
		return new BillingOverview(
				subscriptionRows.stream()
						.map(subscription -> new SubscriptionAdminRow(subscription, userLookup.get(subscription.getUserId())))
						.toList(),
				paymentRows.stream()
						.map(payment -> new PaymentAdminRow(payment, userLookup.get(payment.getUserId())))
						.toList());
	}

	public List<Subscription> subscriptions() {
		return subscriptions.findByPlanTypeAndStatusInOrderByStartDateDesc(PlanType.PAID,
				List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PENDING, SubscriptionStatus.PAST_DUE, SubscriptionStatus.FAILED));
	}

	public List<Subscription> history(String userId) {
		return subscriptions.findByUserIdOrderByStartDateDesc(userId);
	}

	public List<Payment> failedOrPendingPayments() {
		return payments.findByStatusInOrderByCreatedAtDesc(List.of(PaymentStatus.FAILED, PaymentStatus.PENDING));
	}

	public BillingOverview reconcile(String userId) {
		integrations.reconcileBilling(userId);
		return overview();
	}

	private Map<String, BillingUser> usersById(Collection<String> userIds) {
		if (userIds.isEmpty()) return Map.of();
		return users.findAllById(userIds).stream()
				.map(user -> new BillingUser(user.getId(), user.getFullName(), user.getEmail(), user.getStatus()))
				.collect(Collectors.toMap(BillingUser::id, Function.identity()));
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
	public void applyGatewayPaidEvent(String internalToken, String userId, String gatewayReference) {
		validateInternalToken(internalToken);
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

	@Transactional
	public void applyBillingSyncEvent(String internalToken, Requests.BillingSyncRequest request) {
		validateInternalToken(internalToken);
		Subscription subscription = subscriptions.findFirstByUserIdOrderByStartDateDesc(request.userId()).orElseGet(() -> {
			Subscription s = new Subscription();
			s.setId(request.subscriptionId() == null || request.subscriptionId().isBlank()
					? "sub_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12)
					: request.subscriptionId().trim());
			s.setUserId(request.userId());
			s.setStartDate(request.startDate() == null ? Instant.now(clock) : request.startDate());
			return s;
		});
		subscription.setPlanType(request.planType());
		subscription.setStatus(request.subscriptionStatus());
		if (request.renewalDate() != null) subscription.setRenewalDate(request.renewalDate());
		if (request.subscriptionStatus() == SubscriptionStatus.CANCELLED) {
			subscription.setCancelledAt(Instant.now(clock));
		}
		if (request.gatewayReference() != null && !request.gatewayReference().isBlank()) {
			subscription.setGatewayReference(request.gatewayReference().trim());
		}
		subscriptions.save(subscription);

		if (request.paymentStatus() != null) {
			Payment payment = findPayment(request.gatewayReference());
			payment.setUserId(request.userId());
			payment.setAmount(request.amount() == null ? BigDecimal.ZERO : request.amount());
			payment.setStatus(request.paymentStatus());
			payment.setGatewayReference(trimToNull(request.gatewayReference()));
			payment.setCreatedAt(request.paymentCreatedAt() == null ? Instant.now(clock) : request.paymentCreatedAt());
			payment.setRetryOrExpiryDate(request.retryOrExpiryDate());
			payments.save(payment);
		}
	}

	private Payment findPayment(String gatewayReference) {
		String clean = trimToNull(gatewayReference);
		if (clean != null) {
			return payments.findByGatewayReference(clean).orElseGet(this::newPayment);
		}
		return newPayment();
	}

	private Payment newPayment() {
		Payment payment = new Payment();
		payment.setId("pay_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
		return payment;
	}

	private void validateInternalToken(String internalToken) {
		String expected = trimToNull(properties.getIntegrations().getBillingSyncSecret());
		if (expected == null || internalToken == null || !MessageDigestEqual.constantTime(internalToken, expected)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "INTERNAL_TOKEN_INVALID", "Internal billing sync token is invalid.");
		}
	}

	private String trimToNull(String value) {
		if (value == null || value.isBlank()) return null;
		return value.trim();
	}

	private static final class MessageDigestEqual {
		static boolean constantTime(String first, String second) {
			byte[] a = first.getBytes(java.nio.charset.StandardCharsets.UTF_8);
			byte[] b = second.getBytes(java.nio.charset.StandardCharsets.UTF_8);
			if (a.length != b.length) return false;
			int result = 0;
			for (int i = 0; i < a.length; i++) result |= a[i] ^ b[i];
			return result == 0;
		}
	}
}
