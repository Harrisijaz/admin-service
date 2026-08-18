package com.smartInvoice.admin_service.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ai_usage_records", indexes = @Index(name = "idx_ai_usage_user_cycle", columnList = "userId,billingCycleStart", unique = true))
public class AiUsageRecord {
	@Id
	private String id;
	@Column(nullable = false)
	private String userId;
	@Column(nullable = false)
	private int count;
	@Column(nullable = false)
	private int failedAttempts;
	@Column(nullable = false)
	private LocalDate billingCycleStart;
	@Column(nullable = false, precision = 12, scale = 4)
	private BigDecimal estimatedCost = BigDecimal.ZERO;

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }
	public int getCount() { return count; }
	public void setCount(int count) { this.count = count; }
	public int getFailedAttempts() { return failedAttempts; }
	public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }
	public LocalDate getBillingCycleStart() { return billingCycleStart; }
	public void setBillingCycleStart(LocalDate billingCycleStart) { this.billingCycleStart = billingCycleStart; }
	public BigDecimal getEstimatedCost() { return estimatedCost; }
	public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }
}
