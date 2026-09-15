package com.smartInvoice.admin_service.dto;

import com.smartInvoice.admin_service.domain.BlogStatus;
import com.smartInvoice.admin_service.domain.CategoryStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class BlogDtos {
	private BlogDtos() {
	}

	public record BlogBlockRequest(
			@NotBlank String type,
			@Min(0) int order,
			@NotNull Map<String, Object> data) {
	}

	public record BlogBlockResponse(String type, int order, Map<String, Object> data) {
	}

	public record ProductLinkRequest(
			@NotBlank @Size(max = 120) String label,
			@NotBlank @Size(max = 500) String href,
			@Min(0) int order) {
	}

	public record ProductLinkResponse(String label, String href, int order) {
	}

	public record CreateBlogPostRequest(
			@NotBlank @Size(min = 5, max = 150) String title,
			@Size(max = 180) String slug,
			@Size(max = 300) String excerpt,
			@Size(max = 60) String metaTitle,
			@Size(max = 300) String metaDescription,
			@Size(max = 1000) String coverImageUrl,
			@Size(max = 180) String coverImageAlt,
			@Size(max = 120) String authorName,
			String categoryId,
			List<String> tagIds,
			List<@Valid BlogBlockRequest> body,
			List<@Valid ProductLinkRequest> productLinks,
			BlogStatus status) {
	}

	public record UpdateBlogPostRequest(
			@NotBlank @Size(min = 5, max = 150) String title,
			@NotBlank @Size(max = 180) String slug,
			@Size(max = 300) String excerpt,
			@Size(max = 60) String metaTitle,
			@Size(max = 300) String metaDescription,
			@Size(max = 1000) String coverImageUrl,
			@Size(max = 180) String coverImageAlt,
			@Size(max = 120) String authorName,
			String categoryId,
			List<String> tagIds,
			List<@Valid BlogBlockRequest> body,
			List<@Valid ProductLinkRequest> productLinks,
			Long version) {
	}

	public record ChangeBlogPostStatusRequest(@NotNull BlogStatus status) {
	}

	public record CreateBlogCategoryRequest(
			@NotBlank @Size(max = 100) String name,
			@Size(max = 140) String slug,
			@Size(max = 500) String description,
			CategoryStatus status) {
	}

	public record UpdateBlogCategoryRequest(
			@NotBlank @Size(max = 100) String name,
			@NotBlank @Size(max = 140) String slug,
			@Size(max = 500) String description,
			@NotNull CategoryStatus status) {
	}

	public record CategoryResponse(String id, String name, String slug, String description, CategoryStatus status,
			long publishedPostCount) {
	}

	public record TagResponse(String id, String name, String slug) {
	}

	public record BlogPostSummaryResponse(String id, String title, String slug, String excerpt, String coverImageUrl,
			String coverImageAlt, String authorName, CategoryResponse category, List<TagResponse> tags,
			String estimatedReadTime, Instant publishedDate, Instant updatedDate) {
	}

	public record BlogPostDetailResponse(String id, String title, String slug, String excerpt, String metaTitle,
			String metaDescription, String coverImageUrl, String coverImageAlt, String authorName,
			CategoryResponse category, List<TagResponse> tags, String estimatedReadTime, Instant publishedDate,
			Instant updatedDate, List<BlogBlockResponse> body, List<ProductLinkResponse> productLinks) {
	}

	public record BlogPostAdminResponse(String id, String title, String slug, String excerpt, String metaTitle,
			String metaDescription, String coverImageUrl, String coverImageAlt, String authorName,
			CategoryResponse category, List<TagResponse> tags, String estimatedReadTime, BlogStatus status,
			Instant publishedDate, Instant createdAt, Instant updatedAt, String createdBy, String updatedBy,
			Instant archivedAt, String archivedBy, Long version, List<BlogBlockResponse> body,
			List<ProductLinkResponse> productLinks) {
	}

	public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages,
			boolean first, boolean last) {
	}

	public record UploadResponse(String url, String altText, Integer width, Integer height, String fileType,
			long fileSize) {
	}

	public record SitemapItemResponse(String slug, Instant updatedDate) {
	}

	public record RevalidateRequest(List<String> paths) {
	}
}
