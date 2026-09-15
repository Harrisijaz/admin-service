package com.smartInvoice.admin_service.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "blog_content_blocks",
		indexes = @Index(name = "idx_blog_content_post_order", columnList = "post_id,blockOrder"))
public class BlogContentBlock {
	@Id
	private String id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private BlogPost post;
	@Column(nullable = false, length = 40)
	private String type;
	@Column(nullable = false)
	private int blockOrder;
	@Lob
	@Column(nullable = false, columnDefinition = "TEXT")
	private String dataJson;

	@PrePersist
	void prePersist() {
		if (id == null) id = "blk_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
	}

	public String getId() { return id; }
	public BlogPost getPost() { return post; }
	public void setPost(BlogPost post) { this.post = post; }
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	public int getBlockOrder() { return blockOrder; }
	public void setBlockOrder(int blockOrder) { this.blockOrder = blockOrder; }
	public String getDataJson() { return dataJson; }
	public void setDataJson(String dataJson) { this.dataJson = dataJson; }
}
