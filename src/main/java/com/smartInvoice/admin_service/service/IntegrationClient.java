package com.smartInvoice.admin_service.service;

import com.smartInvoice.admin_service.config.AdminProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class IntegrationClient {
	private final WebClient authClient;
	private final WebClient userClient;
	private final WebClient paymentClient;
	private final AdminProperties properties;

	public IntegrationClient(WebClient.Builder builder, AdminProperties properties) {
		this.properties = properties;
		this.authClient = builder.clone().baseUrl(properties.getIntegrations().getAuthServiceBaseUrl()).build();
		this.userClient = builder.clone().baseUrl(properties.getIntegrations().getUserServiceBaseUrl()).build();
		this.paymentClient = builder.clone().baseUrl(properties.getIntegrations().getPaymentServiceBaseUrl()).build();
	}

	public void triggerPasswordReset(String email) {
		authClient.post().uri("/auth/forgot-password").bodyValue(Map.of("email", email)).retrieve().toBodilessEntity().block();
	}

	public void cancelSubscription(String userId) {
		paymentClient.post().uri("/internal/admin/users/{userId}/subscription/cancel", userId).retrieve().toBodilessEntity().block();
	}

	public void invalidateUserSessions(String userId) {
		authClient.post().uri("/internal/admin/users/{userId}/sessions/invalidate", userId).retrieve().toBodilessEntity().block();
	}

	public void reconcileBilling(String userId) {
		userClient.post()
				.uri("/internal/admin/billing/users/{userId}/sync", userId)
				.header("X-Internal-Token", properties.getIntegrations().getBillingSyncSecret())
				.retrieve()
				.toBodilessEntity()
				.block();
	}

	public Object invoices(String userId) {
		return userClient.get().uri("/internal/admin/users/{userId}/invoices", userId).retrieve().bodyToMono(Object.class).onErrorReturn(Map.of()).block();
	}

	public Object expenses(String userId) {
		return userClient.get().uri("/internal/admin/users/{userId}/expenses", userId).retrieve().bodyToMono(Object.class).onErrorReturn(Map.of()).block();
	}
}
