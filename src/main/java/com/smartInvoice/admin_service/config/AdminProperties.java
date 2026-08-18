package com.smartInvoice.admin_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin")
public class AdminProperties {
	private final Integrations integrations = new Integrations();

	public Integrations getIntegrations() { return integrations; }

	public static class Integrations {
		private String authServiceBaseUrl = "http://localhost:9090";
		private String userServiceBaseUrl = "http://localhost:9092";
		private String paymentServiceBaseUrl = "http://localhost:9093";
		private String aiServiceBaseUrl = "http://localhost:9095";

		public String getAuthServiceBaseUrl() { return authServiceBaseUrl; }
		public void setAuthServiceBaseUrl(String authServiceBaseUrl) { this.authServiceBaseUrl = authServiceBaseUrl; }
		public String getUserServiceBaseUrl() { return userServiceBaseUrl; }
		public void setUserServiceBaseUrl(String userServiceBaseUrl) { this.userServiceBaseUrl = userServiceBaseUrl; }
		public String getPaymentServiceBaseUrl() { return paymentServiceBaseUrl; }
		public void setPaymentServiceBaseUrl(String paymentServiceBaseUrl) { this.paymentServiceBaseUrl = paymentServiceBaseUrl; }
		public String getAiServiceBaseUrl() { return aiServiceBaseUrl; }
		public void setAiServiceBaseUrl(String aiServiceBaseUrl) { this.aiServiceBaseUrl = aiServiceBaseUrl; }
	}
}
