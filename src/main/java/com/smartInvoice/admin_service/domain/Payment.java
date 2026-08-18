package com.smartInvoice.admin_service.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments", indexes = {
		@Index(name = "idx_payments_user_created", columnList = "userId,createdAt"),
		@Index(name = "idx_payments_status", columnList = "status")
})
public class Payment {
	@Id
	private String id;
	@Column(nullable = false)
	private String userId;
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount = BigDecimal.ZERO;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PaymentStatus status;
	@Column(length = 100)
	private String gatewayReference;
	private Instant createdAt;
	private Instant retryOrExpiryDate;
	private Instant refundedAt;
	@Column(precision = 12, scale = 2)
	private BigDecimal refundedAmount;
	@Column(length = 1000)
	private String refundNote;

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }
	public BigDecimal getAmount() { return amount; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }
	public PaymentStatus getStatus() { return status; }
	public void setStatus(PaymentStatus status) { this.status = status; }
	public String getGatewayReference() { return gatewayReference; }
	public void setGatewayReference(String gatewayReference) { this.gatewayReference = gatewayReference; }
	public Instant getCreatedAt() { return createdAt; }
	public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
	public Instant getRetryOrExpiryDate() { return retryOrExpiryDate; }
	public void setRetryOrExpiryDate(Instant retryOrExpiryDate) { this.retryOrExpiryDate = retryOrExpiryDate; }
	public Instant getRefundedAt() { return refundedAt; }
	public void setRefundedAt(Instant refundedAt) { this.refundedAt = refundedAt; }
	public BigDecimal getRefundedAmount() { return refundedAmount; }
	public void setRefundedAmount(BigDecimal refundedAmount) { this.refundedAmount = refundedAmount; }
	public String getRefundNote() { return refundNote; }
	public void setRefundNote(String refundNote) { this.refundNote = refundNote; }
}
