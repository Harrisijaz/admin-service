package com.smartInvoice.admin_service.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "subscriptions", indexes = {
		@Index(name = "idx_subscriptions_user", columnList = "userId"),
		@Index(name = "idx_subscriptions_status", columnList = "status")
})
public class Subscription {
	@Id
	private String id;
	@Column(nullable = false)
	private String userId;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PlanType planType = PlanType.FREE;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SubscriptionStatus status = SubscriptionStatus.CANCELLED;
	private Instant startDate;
	private Instant renewalDate;
	private Instant cancelledAt;
	@Column(length = 100)
	private String gatewayReference;
	@Column(nullable = false)
	private boolean manualOverride;
	@Column(length = 500)
	private String manualOverrideReason;
	private Instant manualOverrideAt;
	@Column(nullable = false)
	private boolean superseded;

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }
	public PlanType getPlanType() { return planType; }
	public void setPlanType(PlanType planType) { this.planType = planType; }
	public SubscriptionStatus getStatus() { return status; }
	public void setStatus(SubscriptionStatus status) { this.status = status; }
	public Instant getStartDate() { return startDate; }
	public void setStartDate(Instant startDate) { this.startDate = startDate; }
	public Instant getRenewalDate() { return renewalDate; }
	public void setRenewalDate(Instant renewalDate) { this.renewalDate = renewalDate; }
	public Instant getCancelledAt() { return cancelledAt; }
	public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
	public String getGatewayReference() { return gatewayReference; }
	public void setGatewayReference(String gatewayReference) { this.gatewayReference = gatewayReference; }
	public boolean isManualOverride() { return manualOverride; }
	public void setManualOverride(boolean manualOverride) { this.manualOverride = manualOverride; }
	public String getManualOverrideReason() { return manualOverrideReason; }
	public void setManualOverrideReason(String manualOverrideReason) { this.manualOverrideReason = manualOverrideReason; }
	public Instant getManualOverrideAt() { return manualOverrideAt; }
	public void setManualOverrideAt(Instant manualOverrideAt) { this.manualOverrideAt = manualOverrideAt; }
	public boolean isSuperseded() { return superseded; }
	public void setSuperseded(boolean superseded) { this.superseded = superseded; }
}
