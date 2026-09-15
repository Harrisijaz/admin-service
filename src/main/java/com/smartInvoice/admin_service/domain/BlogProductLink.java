package com.smartInvoice.admin_service.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "blog_product_links",
		indexes = @Index(name = "idx_blog_product_link_post_order", columnList = "post_id,linkOrder"))
public class BlogProductLink {
	@Id
	private String id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private BlogPost post;
	@Column(nullable = false, length = 120)
	private String label;
	@Column(nullable = false, length = 500)
	private String href;
	@Column(nullable = false)
	private int linkOrder;

	@PrePersist
	void prePersist() {
		if (id == null) id = "plnk_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
	}

	public String getId() { return id; }
	public BlogPost getPost() { return post; }
	public void setPost(BlogPost post) { this.post = post; }
	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }
	public String getHref() { return href; }
	public void setHref(String href) { this.href = href; }
	public int getLinkOrder() { return linkOrder; }
	public void setLinkOrder(int linkOrder) { this.linkOrder = linkOrder; }
}
