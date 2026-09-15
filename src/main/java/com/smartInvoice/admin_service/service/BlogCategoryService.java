package com.smartInvoice.admin_service.service;

import com.smartInvoice.admin_service.dto.BlogDtos.*;
import com.smartInvoice.admin_service.domain.AdminActionType;
import com.smartInvoice.admin_service.domain.BlogCategory;
import com.smartInvoice.admin_service.domain.BlogStatus;
import com.smartInvoice.admin_service.domain.CategoryStatus;
import com.smartInvoice.admin_service.repo.BlogCategoryRepository;
import com.smartInvoice.admin_service.repo.BlogPostRepository;
import com.smartInvoice.admin_service.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BlogCategoryService {
	private final BlogCategoryRepository categoryRepository;
	private final BlogPostRepository postRepository;
	private final BlogMapper mapper;
	private final BlogSlugService slugService;
	private final AuditService auditService;

	public BlogCategoryService(BlogCategoryRepository categoryRepository, BlogPostRepository postRepository,
			BlogMapper mapper, BlogSlugService slugService, AuditService auditService) {
		this.categoryRepository = categoryRepository;
		this.postRepository = postRepository;
		this.mapper = mapper;
		this.slugService = slugService;
		this.auditService = auditService;
	}

	@Transactional(readOnly = true)
	public List<CategoryResponse> publicCategories() {
		return categoryRepository.findPublicCategories(CategoryStatus.ACTIVE).stream()
				.map(category -> mapper.category(category, postRepository.countByCategoryIdAndStatus(category.getId(), BlogStatus.PUBLISHED)))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<CategoryResponse> adminCategories() {
		return categoryRepository.findAll().stream()
				.map(category -> mapper.category(category, postRepository.countByCategoryIdAndStatus(category.getId(), BlogStatus.PUBLISHED)))
				.toList();
	}

	@Transactional
	public CategoryResponse create(CreateBlogCategoryRequest request, AdminPrincipal admin) {
		String slug = request.slug() == null || request.slug().isBlank()
				? slugService.uniqueSlug(request.name(), categoryRepository::existsBySlug)
				: slugService.normalize(request.slug());
		validateCategory(null, request.name(), slug);
		BlogCategory category = new BlogCategory();
		category.setName(request.name().trim());
		category.setSlug(slug);
		category.setDescription(trim(request.description()));
		category.setStatus(request.status() == null ? CategoryStatus.ACTIVE : request.status());
		BlogCategory saved = categoryRepository.save(category);
		auditService.log(admin, AdminActionType.BLOG_CATEGORY_CREATED, saved.getId(), "Blog category created: " + saved.getSlug());
		return mapper.category(saved, 0);
	}

	@Transactional
	public CategoryResponse update(String id, UpdateBlogCategoryRequest request, AdminPrincipal admin) {
		BlogCategory category = find(id);
		String slug = slugService.normalize(request.slug());
		validateCategory(id, request.name(), slug);
		category.setName(request.name().trim());
		category.setSlug(slug);
		category.setDescription(trim(request.description()));
		category.setStatus(request.status());
		auditService.log(admin, AdminActionType.BLOG_CATEGORY_UPDATED, category.getId(), "Blog category updated: " + slug);
		return mapper.category(category, postRepository.countByCategoryIdAndStatus(category.getId(), BlogStatus.PUBLISHED));
	}

	@Transactional
	public void delete(String id, AdminPrincipal admin) {
		BlogCategory category = find(id);
		if (postRepository.countByCategoryIdAndStatusNot(id, BlogStatus.ARCHIVED) > 0) {
			category.setStatus(CategoryStatus.INACTIVE);
		} else {
			categoryRepository.delete(category);
		}
		auditService.log(admin, AdminActionType.BLOG_CATEGORY_UPDATED, id, "Blog category removed or inactivated.");
	}

	private BlogCategory find(String id) {
		return categoryRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BLOG_CATEGORY_NOT_FOUND", "Blog category not found."));
	}

	private void validateCategory(String id, String name, String slug) {
		if (!slugService.isValidSlug(slug)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_INVALID_CATEGORY_SLUG", "Category slug is invalid.");
		}
		boolean duplicateName = id == null ? categoryRepository.existsByNameIgnoreCase(name.trim())
				: categoryRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), id);
		if (duplicateName) throw new ApiException(HttpStatus.CONFLICT, "BLOG_DUPLICATE_CATEGORY", "Category name already exists.");
		boolean duplicateSlug = id == null ? categoryRepository.existsBySlug(slug)
				: categoryRepository.existsBySlugAndIdNot(slug, id);
		if (duplicateSlug) throw new ApiException(HttpStatus.CONFLICT, "BLOG_DUPLICATE_CATEGORY", "Category slug already exists.");
	}

	private String trim(String value) {
		return value == null ? null : value.trim();
	}
}
