package com.smartInvoice.admin_service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartInvoice.admin_service.dto.BlogDtos.*;
import com.smartInvoice.admin_service.domain.*;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class BlogMapper {
	private final ObjectMapper objectMapper;
	private final BlogPostServiceSupport support;

	public BlogMapper(ObjectMapper objectMapper, BlogPostServiceSupport support) {
		this.objectMapper = objectMapper;
		this.support = support;
	}

	public BlogPostSummaryResponse summary(BlogPost post) {
		return new BlogPostSummaryResponse(post.getId(), post.getTitle(), post.getSlug(), post.getExcerpt(),
				post.getCoverImageUrl(), post.getCoverImageAlt(), post.getAuthorName(), category(post.getCategory(), 0),
				tags(post.getTags().stream().toList()), post.getEstimatedReadTime() + " min read",
				post.getPublishedDate(), post.getUpdatedAt());
	}

	public BlogPostDetailResponse detail(BlogPost post) {
		return new BlogPostDetailResponse(post.getId(), post.getTitle(), post.getSlug(), post.getExcerpt(),
				post.getMetaTitle(), post.getMetaDescription(), post.getCoverImageUrl(), post.getCoverImageAlt(),
				post.getAuthorName(), category(post.getCategory(), 0), tags(post.getTags().stream().toList()),
				post.getEstimatedReadTime() + " min read", post.getPublishedDate(), post.getUpdatedAt(),
				blocks(post.getBody()), productLinks(post.getProductLinks()));
	}

	public BlogPostAdminResponse admin(BlogPost post) {
		return new BlogPostAdminResponse(post.getId(), post.getTitle(), post.getSlug(), post.getExcerpt(),
				post.getMetaTitle(), post.getMetaDescription(), post.getCoverImageUrl(), post.getCoverImageAlt(),
				post.getAuthorName(), category(post.getCategory(), 0), tags(post.getTags().stream().toList()),
				post.getEstimatedReadTime() + " min read", post.getStatus(), post.getPublishedDate(),
				post.getCreatedAt(), post.getUpdatedAt(), post.getCreatedBy(), post.getUpdatedBy(),
				post.getArchivedAt(), post.getArchivedBy(), post.getVersion(), blocks(post.getBody()),
				productLinks(post.getProductLinks()));
	}

	public CategoryResponse category(BlogCategory category, long count) {
		if (category == null) return null;
		return new CategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getDescription(),
				category.getStatus(), count);
	}

	public List<TagResponse> tags(List<BlogTag> tags) {
		return tags.stream()
				.sorted(Comparator.comparing(BlogTag::getName, String.CASE_INSENSITIVE_ORDER))
				.map(tag -> new TagResponse(tag.getId(), tag.getName(), tag.getSlug()))
				.toList();
	}

	public List<BlogBlockResponse> blocks(List<BlogContentBlock> blocks) {
		return blocks.stream()
				.sorted(Comparator.comparingInt(BlogContentBlock::getBlockOrder))
				.map(block -> new BlogBlockResponse(block.getType(), block.getBlockOrder(), readMap(block.getDataJson())))
				.toList();
	}

	public List<ProductLinkResponse> productLinks(List<BlogProductLink> links) {
		return links.stream()
				.sorted(Comparator.comparingInt(BlogProductLink::getLinkOrder))
				.map(link -> new ProductLinkResponse(link.getLabel(), link.getHref(), link.getLinkOrder()))
				.toList();
	}

	private Map<String, Object> readMap(String json) {
		return support.readMap(json);
	}
}

@Component
class BlogPostServiceSupport {
	private final ObjectMapper objectMapper;

	BlogPostServiceSupport(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	Map<String, Object> readMap(String json) {
		try {
			return objectMapper.readValue(json, new TypeReference<>() {});
		} catch (Exception ex) {
			return Map.of();
		}
	}
}
