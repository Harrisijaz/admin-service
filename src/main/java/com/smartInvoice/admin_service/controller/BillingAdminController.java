package com.smartInvoice.admin_service.controller;

import com.smartInvoice.admin_service.domain.Payment;
import com.smartInvoice.admin_service.domain.Subscription;
import com.smartInvoice.admin_service.dto.Requests;
import com.smartInvoice.admin_service.service.AdminPrincipal;
import com.smartInvoice.admin_service.service.BillingAdminService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/billing")
public class BillingAdminController {
	private final BillingAdminService service;

	public BillingAdminController(BillingAdminService service) {
		this.service = service;
	}

	@GetMapping("/users/{userId}/subscriptions")
	public List<Subscription> history(@PathVariable String userId) {
		return service.history(userId);
	}

	@PostMapping("/users/{userId}/plan")
	public Subscription changePlan(@PathVariable String userId, @Valid @RequestBody Requests.ChangePlanRequest request,
			@AuthenticationPrincipal AdminPrincipal admin) {
		return service.changePlan(userId, request, admin);
	}

	@GetMapping("/payments/failed-pending")
	public List<Payment> failedOrPendingPayments() {
		return service.failedOrPendingPayments();
	}

	@PostMapping("/payments/{paymentId}/refund")
	public Payment refund(@PathVariable String paymentId, @Valid @RequestBody Requests.RefundRequest request,
			@AuthenticationPrincipal AdminPrincipal admin) {
		return service.refund(paymentId, request, admin);
	}

	@PostMapping("/internal/webhooks/payment-succeeded")
	public void paymentSucceeded(@RequestParam String userId, @RequestParam String gatewayReference) {
		service.applyGatewayPaidEvent(userId, gatewayReference);
	}
}
