package com.smartInvoice.admin_service.controller;

import com.smartInvoice.admin_service.domain.BlogStatus;
import com.smartInvoice.admin_service.dto.BlogDtos.*;
import com.smartInvoice.admin_service.service.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/blog")
public class BlogAdminController {
	private final BlogPostService postService;
	private final BlogCategoryService categoryService;
	private final BlogTagService tagService;
	private final BlogMediaService mediaService;
	private final BlogRevalidationService revalidationService;

	public BlogAdminController(BlogPostService postService, BlogCategoryService categoryService,
			BlogTagService tagService, BlogMediaService mediaService, BlogRevalidationService revalidationService) {
		this.postService = postService;
		this.categoryService = categoryService;
		this.tagService = tagService;
		this.mediaService = mediaService;
		this.revalidationService = revalidationService;
	}

	@GetMapping("/posts")
	public PageResponse<BlogPostAdminResponse> posts(@RequestParam(required = false) BlogStatus status,
			@RequestParam(required = false) String category,
			@RequestParam(required = false) String tag,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String createdBy,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant publishedFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant publishedTo,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size,
			@RequestParam(required = false) String sort) {
		return postService.adminList(status, category, tag, search, createdBy, publishedFrom, publishedTo, page, size, sort);
	}

	@GetMapping("/posts/{id}")
	public BlogPostAdminResponse post(@PathVariable String id) {
		return postService.adminDetail(id);
	}

	@PostMapping("/posts")
	@ResponseStatus(HttpStatus.CREATED)
	public BlogPostAdminResponse create(@Valid @RequestBody CreateBlogPostRequest request,
			@AuthenticationPrincipal AdminPrincipal admin) {
		return postService.create(request, admin);
	}

	@PutMapping("/posts/{id}")
	public BlogPostAdminResponse update(@PathVariable String id, @Valid @RequestBody UpdateBlogPostRequest request,
			@AuthenticationPrincipal AdminPrincipal admin) {
		return postService.update(id, request, admin);
	}

	@PatchMapping("/posts/{id}/status")
	public BlogPostAdminResponse status(@PathVariable String id, @Valid @RequestBody ChangeBlogPostStatusRequest request,
			@AuthenticationPrincipal AdminPrincipal admin) {
		return postService.changeStatus(id, request, admin);
	}

	@DeleteMapping("/posts/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void archive(@PathVariable String id, @AuthenticationPrincipal AdminPrincipal admin) {
		postService.archive(id, admin);
	}

	@GetMapping("/categories")
	public List<CategoryResponse> categories() {
		return categoryService.adminCategories();
	}

	@PostMapping("/categories")
	@ResponseStatus(HttpStatus.CREATED)
	public CategoryResponse createCategory(@Valid @RequestBody CreateBlogCategoryRequest request,
			@AuthenticationPrincipal AdminPrincipal admin) {
		return categoryService.create(request, admin);
	}

	@PutMapping("/categories/{id}")
	public CategoryResponse updateCategory(@PathVariable String id, @Valid @RequestBody UpdateBlogCategoryRequest request,
			@AuthenticationPrincipal AdminPrincipal admin) {
		return categoryService.update(id, request, admin);
	}

	@DeleteMapping("/categories/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteCategory(@PathVariable String id, @AuthenticationPrincipal AdminPrincipal admin) {
		categoryService.delete(id, admin);
	}

	@GetMapping("/tags")
	public List<TagResponse> tags() {
		return tagService.list();
	}

	@PostMapping("/tags")
	@ResponseStatus(HttpStatus.CREATED)
	public TagResponse createTag(@RequestBody Map<String, String> request) {
		return tagService.create(request.get("name"));
	}

	@PostMapping("/upload")
	public UploadResponse upload(@RequestPart("file") MultipartFile file,
			@RequestParam(required = false) String altText,
			@AuthenticationPrincipal AdminPrincipal admin) {
		return mediaService.upload(file, altText, admin);
	}

	@PostMapping("/revalidate")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void revalidate(@RequestBody RevalidateRequest request) {
		revalidationService.afterCommit(request.paths());
	}
}
