package com.smartInvoice.admin_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "users", indexes = {
		@Index(name = "idx_users_admin_search", columnList = "fullName,email_normalized"),
		@Index(name = "idx_users_admin_status_created", columnList = "status,createdAt")
})
public class User {
	@Id
	private String id;

	@Column(nullable = false, length = 254)
	private String email;

	@Column(name = "email_normalized", nullable = false, length = 254)
	private String emailNormalized;

	@Column(nullable = false, length = 100)
	private String fullName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AccountStatus status = AccountStatus.UNVERIFIED;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	private Instant deletedAt;

	@Column(nullable = false)
	private boolean emailVerified;

	@Column
	private boolean trusted;

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getEmailNormalized() { return emailNormalized; }
	public void setEmailNormalized(String emailNormalized) { this.emailNormalized = emailNormalized; }
	public String getFullName() { return fullName; }
	public void setFullName(String fullName) { this.fullName = fullName; }
	public AccountStatus getStatus() { return status; }
	public void setStatus(AccountStatus status) { this.status = status; }
	public Instant getCreatedAt() { return createdAt; }
	public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
	public Instant getDeletedAt() { return deletedAt; }
	public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
	public boolean isEmailVerified() { return emailVerified; }
	public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
	public boolean isTrusted() { return trusted; }
	public void setTrusted(boolean trusted) { this.trusted = trusted; }
}
