package com.smartInvoice.admin_service.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "blog_posts",
		uniqueConstraints = @UniqueConstraint(name = "uk_blog_post_slug", columnNames = "slug"),
		indexes = {
				@Index(name = "idx_blog_post_slug", columnList = "slug"),
				@Index(name = "idx_blog_post_status", columnList = "status"),
				@Index(name = "idx_blog_post_published", columnList = "publishedDate"),
				@Index(name = "idx_blog_post_category", columnList = "category_id"),
				@Index(name = "idx_blog_post_created", columnList = "createdAt"),
				@Index(name = "idx_blog_post_status_published", columnList = "status,publishedDate")
		})
public class BlogPost {
	@Id
	private String id;
	@Column(nullable = false, length = 150)
	private String title;
	@Column(nullable = false, length = 180)
	private String slug;
	@Column(length = 300)
	private String excerpt;
	@Column(length = 60)
	private String metaTitle;
	@Column(length = 300)
	private String metaDescription;
	@Column(length = 1000)
	private String coverImageUrl;
	@Column(length = 180)
	private String coverImageAlt;
	@Column(length = 120)
	private String authorName;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private BlogCategory category;
	@ManyToMany
	@JoinTable(name = "blog_post_tags",
			joinColumns = @JoinColumn(name = "post_id"),
			inverseJoinColumns = @JoinColumn(name = "tag_id"),
			uniqueConstraints = @UniqueConstraint(name = "uk_blog_post_tag", columnNames = {"post_id", "tag_id"}))
	private Set<BlogTag> tags = new LinkedHashSet<>();
	@Column(nullable = false)
	private int estimatedReadTime;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BlogStatus status = BlogStatus.DRAFT;
	private Instant publishedDate;
	@Column(nullable = false, updatable = false)
	private Instant createdAt;
	@Column(nullable = false)
	private Instant updatedAt;
	@Column(nullable = false, updatable = false, length = 120)
	private String createdBy;
	@Column(nullable = false, length = 120)
	private String updatedBy;
	private Instant archivedAt;
	@Column(length = 120)
	private String archivedBy;
	@Version
	private Long version;
	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("blockOrder asc")
	private List<BlogContentBlock> body = new ArrayList<>();
	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("linkOrder asc")
	private List<BlogProductLink> productLinks = new ArrayList<>();

	@PrePersist
	void prePersist() {
		if (id == null) id = "post_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
		Instant now = Instant.now();
		if (createdAt == null) createdAt = now;
		if (updatedAt == null) updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = Instant.now();
	}

	public void replaceBody(List<BlogContentBlock> blocks) {
		body.clear();
		blocks.forEach(block -> {
			block.setPost(this);
			body.add(block);
		});
	}

	public void replaceProductLinks(List<BlogProductLink> links) {
		productLinks.clear();
		links.forEach(link -> {
			link.setPost(this);
			productLinks.add(link);
		});
	}

	public String getId() { return id; }
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public String getSlug() { return slug; }
	public void setSlug(String slug) { this.slug = slug; }
	public String getExcerpt() { return excerpt; }
	public void setExcerpt(String excerpt) { this.excerpt = excerpt; }
	public String getMetaTitle() { return metaTitle; }
	public void setMetaTitle(String metaTitle) { this.metaTitle = metaTitle; }
	public String getMetaDescription() { return metaDescription; }
	public void setMetaDescription(String metaDescription) { this.metaDescription = metaDescription; }
	public String getCoverImageUrl() { return coverImageUrl; }
	public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
	public String getCoverImageAlt() { return coverImageAlt; }
	public void setCoverImageAlt(String coverImageAlt) { this.coverImageAlt = coverImageAlt; }
	public String getAuthorName() { return authorName; }
	public void setAuthorName(String authorName) { this.authorName = authorName; }
	public BlogCategory getCategory() { return category; }
	public void setCategory(BlogCategory category) { this.category = category; }
	public Set<BlogTag> getTags() { return tags; }
	public void setTags(Set<BlogTag> tags) { this.tags = tags; }
	public int getEstimatedReadTime() { return estimatedReadTime; }
	public void setEstimatedReadTime(int estimatedReadTime) { this.estimatedReadTime = estimatedReadTime; }
	public BlogStatus getStatus() { return status; }
	public void setStatus(BlogStatus status) { this.status = status; }
	public Instant getPublishedDate() { return publishedDate; }
	public void setPublishedDate(Instant publishedDate) { this.publishedDate = publishedDate; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
	public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	public String getUpdatedBy() { return updatedBy; }
	public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
	public Instant getArchivedAt() { return archivedAt; }
	public void setArchivedAt(Instant archivedAt) { this.archivedAt = archivedAt; }
	public String getArchivedBy() { return archivedBy; }
	public void setArchivedBy(String archivedBy) { this.archivedBy = archivedBy; }
	public Long getVersion() { return version; }
	public List<BlogContentBlock> getBody() { return body; }
	public List<BlogProductLink> getProductLinks() { return productLinks; }
}
