package com.smartInvoice.admin_service.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "blog_tags",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_blog_tag_name", columnNames = "name"),
				@UniqueConstraint(name = "uk_blog_tag_slug", columnNames = "slug")
		})
public class BlogTag {
	@Id
	private String id;
	@Column(nullable = false, length = 80)
	private String name;
	@Column(nullable = false, length = 120)
	private String slug;

	@PrePersist
	void prePersist() {
		if (id == null) id = "tag_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
	}

	public String getId() { return id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getSlug() { return slug; }
	public void setSlug(String slug) { this.slug = slug; }
}
