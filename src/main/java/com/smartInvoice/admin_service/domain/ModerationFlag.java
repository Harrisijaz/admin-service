package com.smartInvoice.admin_service.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "moderation_flags", indexes = @Index(name = "idx_moderation_flags_status", columnList = "status,createdAt"))
public class ModerationFlag {
	@Id
	private String id;
	@Column(nullable = false)
	private String userId;
	@Column(nullable = false, length = 500)
	private String reason;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private FlagStatus status = FlagStatus.OPEN;
	@Column(length = 300)
	private String dismissalNote;
	private String dismissedByAdminId;
	private Instant dismissedAt;
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void prePersist() {
		if (id == null) id = "flag_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		if (createdAt == null) createdAt = Instant.now();
	}

	public String getId() { return id; }
	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }
	public String getReason() { return reason; }
	public void setReason(String reason) { this.reason = reason; }
	public FlagStatus getStatus() { return status; }
	public void setStatus(FlagStatus status) { this.status = status; }
	public String getDismissalNote() { return dismissalNote; }
	public void setDismissalNote(String dismissalNote) { this.dismissalNote = dismissalNote; }
	public String getDismissedByAdminId() { return dismissedByAdminId; }
	public void setDismissedByAdminId(String dismissedByAdminId) { this.dismissedByAdminId = dismissedByAdminId; }
	public Instant getDismissedAt() { return dismissedAt; }
	public void setDismissedAt(Instant dismissedAt) { this.dismissedAt = dismissedAt; }
	public Instant getCreatedAt() { return createdAt; }
}
