package com.smartInvoice.admin_service.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admin_activity_logs", indexes = @Index(name = "idx_admin_activity_created", columnList = "createdAt"))
public class AdminActivityLog {
	@Id
	private String id;
	@Column(nullable = false)
	private String adminId;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private AdminActionType actionType;
	private String targetUserId;
	@Column(nullable = false, length = 1000)
	private String description;
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void prePersist() {
		if (id == null) id = "alog_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		if (createdAt == null) createdAt = Instant.now();
	}

	public String getId() { return id; }
	public String getAdminId() { return adminId; }
	public void setAdminId(String adminId) { this.adminId = adminId; }
	public AdminActionType getActionType() { return actionType; }
	public void setActionType(AdminActionType actionType) { this.actionType = actionType; }
	public String getTargetUserId() { return targetUserId; }
	public void setTargetUserId(String targetUserId) { this.targetUserId = targetUserId; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public Instant getCreatedAt() { return createdAt; }
}
