package com.smartInvoice.admin_service.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "blog_post_slug_history",
		uniqueConstraints = @UniqueConstraint(name = "uk_blog_old_slug", columnNames = "oldSlug"),
		indexes = @Index(name = "idx_blog_slug_history_post", columnList = "post_id"))
public class BlogSlugHistory {
	@Id
	private String id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private BlogPost post;
	@Column(nullable = false, length = 180)
	private String oldSlug;
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void prePersist() {
		if (id == null) id = "slg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
		if (createdAt == null) createdAt = Instant.now();
	}

	public String getId() { return id; }
	public BlogPost getPost() { return post; }
	public void setPost(BlogPost post) { this.post = post; }
	public String getOldSlug() { return oldSlug; }
	public void setOldSlug(String oldSlug) { this.oldSlug = oldSlug; }
	public Instant getCreatedAt() { return createdAt; }
}
