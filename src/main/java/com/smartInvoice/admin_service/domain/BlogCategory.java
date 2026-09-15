package com.smartInvoice.admin_service.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "blog_categories",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_blog_category_name", columnNames = "name"),
				@UniqueConstraint(name = "uk_blog_category_slug", columnNames = "slug")
		},
		indexes = @Index(name = "idx_blog_category_status", columnList = "status"))
public class BlogCategory {
	@Id
	private String id;
	@Column(nullable = false, length = 100)
	private String name;
	@Column(nullable = false, length = 140)
	private String slug;
	@Column(length = 500)
	private String description;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CategoryStatus status = CategoryStatus.ACTIVE;
	@Column(nullable = false, updatable = false)
	private Instant createdAt;
	@Column(nullable = false)
	private Instant updatedAt;

	@PrePersist
	void prePersist() {
		if (id == null) id = "cat_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
		Instant now = Instant.now();
		if (createdAt == null) createdAt = now;
		if (updatedAt == null) updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = Instant.now();
	}

	public String getId() { return id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getSlug() { return slug; }
	public void setSlug(String slug) { this.slug = slug; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public CategoryStatus getStatus() { return status; }
	public void setStatus(CategoryStatus status) { this.status = status; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
