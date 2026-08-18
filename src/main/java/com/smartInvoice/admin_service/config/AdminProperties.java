package com.smartInvoice.admin_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin")
public class AdminProperties {
	private final Auth auth = new Auth();
	private final Integrations integrations = new Integrations();

	public Auth getAuth() { return auth; }
	public Integrations getIntegrations() { return integrations; }

	public static class Auth {
		private String issuer = "smart-invoice-auth";
		private String jwtKeyId = "smart-invoice-auth-key";
		private String jwtPublicKey = "";
		private String jweSecret = "";
		private boolean tokenEncryptionEnabled = true;
		private int inactivityTimeoutMinutes = 20;

		public String getIssuer() { return issuer; }
		public void setIssuer(String issuer) { this.issuer = issuer; }
		public String getJwtKeyId() { return jwtKeyId; }
		public void setJwtKeyId(String jwtKeyId) { this.jwtKeyId = jwtKeyId; }
		public String getJwtPublicKey() { return jwtPublicKey; }
		public void setJwtPublicKey(String jwtPublicKey) { this.jwtPublicKey = jwtPublicKey; }
		public String getJweSecret() { return jweSecret; }
		public void setJweSecret(String jweSecret) { this.jweSecret = jweSecret; }
		public boolean isTokenEncryptionEnabled() { return tokenEncryptionEnabled; }
		public void setTokenEncryptionEnabled(boolean tokenEncryptionEnabled) { this.tokenEncryptionEnabled = tokenEncryptionEnabled; }
		public int getInactivityTimeoutMinutes() { return inactivityTimeoutMinutes; }
		public void setInactivityTimeoutMinutes(int inactivityTimeoutMinutes) { this.inactivityTimeoutMinutes = inactivityTimeoutMinutes; }
	}

	public static class Integrations {
		private String authServiceBaseUrl = "http://localhost:9090";
		private String userServiceBaseUrl = "http://localhost:9091";
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
