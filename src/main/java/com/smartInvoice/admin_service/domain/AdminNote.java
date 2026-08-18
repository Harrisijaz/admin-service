package com.smartInvoice.admin_service.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admin_notes", indexes = @Index(name = "idx_admin_notes_user", columnList = "userId,createdAt"))
public class AdminNote {
	@Id
	private String id;
	@Column(nullable = false)
	private String userId;
	@Column(nullable = false)
	private String adminId;
	@Column(nullable = false, length = 1000)
	private String noteText;
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void prePersist() {
		if (id == null) id = "note_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		if (createdAt == null) createdAt = Instant.now();
	}

	public String getId() { return id; }
	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }
	public String getAdminId() { return adminId; }
	public void setAdminId(String adminId) { this.adminId = adminId; }
	public String getNoteText() { return noteText; }
	public void setNoteText(String noteText) { this.noteText = noteText; }
	public Instant getCreatedAt() { return createdAt; }
}
