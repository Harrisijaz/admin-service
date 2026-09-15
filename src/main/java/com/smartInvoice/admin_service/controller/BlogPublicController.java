package com.smartInvoice.admin_service.controller;

import com.smartInvoice.admin_service.dto.BlogDtos.*;
import com.smartInvoice.admin_service.service.BlogCategoryService;
import com.smartInvoice.admin_service.service.BlogPostService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/blog")
public class BlogPublicController {
	private final BlogPostService postService;
	private final BlogCategoryService categoryService;

	public BlogPublicController(BlogPostService postService, BlogCategoryService categoryService) {
		this.postService = postService;
		this.categoryService = categoryService;
	}

	@GetMapping("/posts")
	public PageResponse<BlogPostSummaryResponse> posts(@RequestParam(required = false) @Min(0) Integer page,
			@RequestParam(required = false) @Min(1) @Max(50) Integer size,
			@RequestParam(required = false) String category,
			@RequestParam(required = false) String tag,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String sort) {
		return postService.publicList(page, size, category, tag, search, sort);
	}

	@GetMapping("/posts/{slug}")
	public BlogPostDetailResponse post(@PathVariable String slug) {
		return postService.publicDetail(slug);
	}

	@GetMapping("/categories")
	public List<CategoryResponse> categories() {
		return categoryService.publicCategories();
	}

	@GetMapping("/sitemap")
	public List<SitemapItemResponse> sitemap() {
		return postService.sitemap();
	}
}
