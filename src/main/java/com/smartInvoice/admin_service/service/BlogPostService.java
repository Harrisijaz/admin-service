package com.smartInvoice.admin_service.service;

import com.smartInvoice.admin_service.dto.BlogDtos.*;
import com.smartInvoice.admin_service.domain.*;
import com.smartInvoice.admin_service.repo.BlogCategoryRepository;
import com.smartInvoice.admin_service.repo.BlogPostRepository;
import com.smartInvoice.admin_service.repo.BlogSlugHistoryRepository;
import com.smartInvoice.admin_service.repo.BlogTagRepository;
import com.smartInvoice.admin_service.web.ApiException;
import com.smartInvoice.admin_service.web.BlogPublishValidationException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

import static com.smartInvoice.admin_service.service.BlogSpecifications.*;

@Service
public class BlogPostService {
	private final BlogPostRepository postRepository;
	private final BlogCategoryRepository categoryRepository;
	private final BlogTagRepository tagRepository;
	private final BlogSlugHistoryRepository slugHistoryRepository;
	private final BlogMapper mapper;
	private final BlogSlugService slugService;
	private final BlogContentValidator contentValidator;
	private final BlogReadTimeCalculator readTimeCalculator;
	private final BlogPageSupport pageSupport;
	private final BlogProperties properties;
	private final AuditService auditService;
	private final BlogRevalidationService revalidationService;

	public BlogPostService(BlogPostRepository postRepository, BlogCategoryRepository categoryRepository,
			BlogTagRepository tagRepository, BlogSlugHistoryRepository slugHistoryRepository, BlogMapper mapper,
			BlogSlugService slugService, BlogContentValidator contentValidator, BlogReadTimeCalculator readTimeCalculator,
			BlogPageSupport pageSupport, BlogProperties properties, AuditService auditService,
			BlogRevalidationService revalidationService) {
		this.postRepository = postRepository;
		this.categoryRepository = categoryRepository;
		this.tagRepository = tagRepository;
		this.slugHistoryRepository = slugHistoryRepository;
		this.mapper = mapper;
		this.slugService = slugService;
		this.contentValidator = contentValidator;
		this.readTimeCalculator = readTimeCalculator;
		this.pageSupport = pageSupport;
		this.properties = properties;
		this.auditService = auditService;
		this.revalidationService = revalidationService;
	}

	@Transactional(readOnly = true)
	public PageResponse<BlogPostSummaryResponse> publicList(Integer page, Integer size, String category, String tag,
			String search, String sort) {
		Page<BlogPost> result = postRepository.findAll(
				status(BlogStatus.PUBLISHED).and(categorySlug(category)).and(tagSlug(tag)).and(search(search)),
				pageSupport.pageable(page, size, sort, "publishedDate,desc"));
		return page(result.map(mapper::summary));
	}

	@Transactional(readOnly = true)
	public BlogPostDetailResponse publicDetail(String slug) {
		return postRepository.findDetailedBySlugAndStatus(slug, BlogStatus.PUBLISHED)
				.map(mapper::detail)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BLOG_POST_NOT_FOUND", "Blog post not found."));
	}

	@Transactional(readOnly = true)
	public List<SitemapItemResponse> sitemap() {
		return postRepository.findPublishedForSitemap().stream()
				.map(post -> new SitemapItemResponse(post.getSlug(), post.getUpdatedAt()))
				.toList();
	}

	@Transactional(readOnly = true)
	public PageResponse<BlogPostAdminResponse> adminList(BlogStatus statusValue, String categoryId, String tagId,
			String searchValue, String createdByValue, Instant publishedFrom, Instant publishedTo, Integer page,
			Integer size, String sortValue) {
		Specification<BlogPost> spec = status(statusValue).and(categoryId(categoryId)).and(tagId(tagId))
				.and(search(searchValue)).and(createdBy(createdByValue)).and(publishedBetween(publishedFrom, publishedTo));
		Page<BlogPost> result = postRepository.findAll(spec, pageSupport.pageable(page, size, sortValue, "createdAt,desc"));
		return page(result.map(mapper::admin));
	}

	@Transactional(readOnly = true)
	public BlogPostAdminResponse adminDetail(String id) {
		return mapper.admin(findDetailed(id));
	}

	@Transactional
	public BlogPostAdminResponse create(CreateBlogPostRequest request, AdminPrincipal admin) {
		BlogPost post = new BlogPost();
		String slug = request.slug() == null || request.slug().isBlank()
				? slugService.uniqueSlug(request.title(), this::slugTaken)
				: normalizeManualSlug(request.slug());
		if (postRepository.existsBySlug(slug) || slugHistoryRepository.existsByOldSlug(slug)) {
			throw new ApiException(HttpStatus.CONFLICT, "BLOG_DUPLICATE_SLUG", "Blog slug already exists.");
		}
		apply(post, request.title(), slug, request.excerpt(), request.metaTitle(), request.metaDescription(),
				request.coverImageUrl(), request.coverImageAlt(), request.authorName(), request.categoryId(),
				request.tagIds(), request.body(), request.productLinks());
		post.setCreatedBy(admin.id());
		post.setUpdatedBy(admin.id());
		BlogStatus requested = request.status() == null ? BlogStatus.DRAFT : request.status();
		if (requested == BlogStatus.PUBLISHED) publish(post);
		else if (requested == BlogStatus.ARCHIVED) archive(post, admin);
		else if (requested != BlogStatus.DRAFT) throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_INVALID_STATUS", "New posts must be draft, published, or archived.");
		BlogPost saved = postRepository.save(post);
		auditService.log(admin, AdminActionType.BLOG_CREATED, saved.getId(), "Blog post created: " + saved.getSlug());
		if (saved.getStatus() == BlogStatus.PUBLISHED) revalidateServiceFor(saved, null);
		return mapper.admin(saved);
	}

	@Transactional
	public BlogPostAdminResponse update(String id, UpdateBlogPostRequest request, AdminPrincipal admin) {
		try {
			BlogPost post = findDetailed(id);
			if (request.version() != null && !request.version().equals(post.getVersion())) {
				throw conflict();
			}
			String oldSlug = post.getSlug();
			String newSlug = normalizeManualSlug(request.slug());
			if ((!oldSlug.equals(newSlug) && postRepository.existsBySlug(newSlug)) || slugHistoryRepository.existsByOldSlug(newSlug)) {
				throw new ApiException(HttpStatus.CONFLICT, "BLOG_DUPLICATE_SLUG", "Blog slug already exists or is reserved.");
			}
			apply(post, request.title(), newSlug, request.excerpt(), request.metaTitle(), request.metaDescription(),
					request.coverImageUrl(), request.coverImageAlt(), request.authorName(), request.categoryId(),
					request.tagIds(), request.body(), request.productLinks());
			post.setUpdatedBy(admin.id());
			if (post.getStatus() == BlogStatus.PUBLISHED) validatePublish(post);
			if (!oldSlug.equals(newSlug)) {
				BlogSlugHistory history = new BlogSlugHistory();
				history.setPost(post);
				history.setOldSlug(oldSlug);
				slugHistoryRepository.save(history);
			}
			auditService.log(admin, AdminActionType.BLOG_UPDATED, post.getId(), "Blog post updated: " + post.getSlug());
			if (post.getStatus() == BlogStatus.PUBLISHED || !oldSlug.equals(newSlug)) revalidateServiceFor(post, oldSlug);
			return mapper.admin(post);
		} catch (OptimisticLockException | OptimisticLockingFailureException ex) {
			throw conflict();
		}
	}

	@Transactional
	public BlogPostAdminResponse changeStatus(String id, ChangeBlogPostStatusRequest request, AdminPrincipal admin) {
		BlogPost post = findDetailed(id);
		BlogStatus from = post.getStatus();
		BlogStatus to = request.status();
		if (from == to) return mapper.admin(post);
		if (!allowed(from, to)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_INVALID_STATUS_TRANSITION", "Blog status transition is not allowed.");
		}
		if (to == BlogStatus.PUBLISHED) publish(post);
		else if (to == BlogStatus.ARCHIVED) archive(post, admin);
		else if (to == BlogStatus.UNPUBLISHED) post.setStatus(BlogStatus.UNPUBLISHED);
		else if (to == BlogStatus.DRAFT) {
			post.setStatus(BlogStatus.DRAFT);
			post.setArchivedAt(null);
			post.setArchivedBy(null);
		}
		post.setUpdatedBy(admin.id());
		auditStatus(admin, post, to);
		revalidateServiceFor(post, null);
		return mapper.admin(post);
	}

	@Transactional
	public void archive(String id, AdminPrincipal admin) {
		BlogPost post = findDetailed(id);
		if (post.getStatus() != BlogStatus.ARCHIVED) {
			archive(post, admin);
			post.setUpdatedBy(admin.id());
			auditService.log(admin, AdminActionType.BLOG_ARCHIVED, post.getId(), "Blog post archived: " + post.getSlug());
			revalidateServiceFor(post, null);
		}
	}

	private void apply(BlogPost post, String title, String slug, String excerpt, String metaTitle, String metaDescription,
			String coverImageUrl, String coverImageAlt, String authorName, String categoryId, List<String> tagIds,
			List<BlogBlockRequest> body, List<ProductLinkRequest> productLinks) {
		post.setTitle(title.trim());
		post.setSlug(slug);
		post.setExcerpt(trim(excerpt));
		post.setMetaTitle(trim(metaTitle));
		post.setMetaDescription(trim(metaDescription));
		post.setCoverImageUrl(trim(coverImageUrl));
		post.setCoverImageAlt(trim(coverImageAlt));
		post.setAuthorName(trim(authorName));
		post.setCategory(categoryId == null || categoryId.isBlank() ? null : categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BLOG_CATEGORY_NOT_FOUND", "Blog category not found.")));
		post.setTags(loadTags(tagIds));
		post.replaceBody(contentValidator.toBlocks(body));
		post.replaceProductLinks(contentValidator.toProductLinks(productLinks));
		post.setEstimatedReadTime(readTimeCalculator.calculate(post.getBody()));
		if (post.getCoverImageUrl() != null && !post.getCoverImageUrl().isBlank()
				&& (post.getCoverImageAlt() == null || post.getCoverImageAlt().isBlank())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_IMAGE_ALT_REQUIRED", "Cover image alt text is required when cover image exists.");
		}
	}

	private Set<BlogTag> loadTags(List<String> tagIds) {
		if (tagIds == null || tagIds.isEmpty()) return new LinkedHashSet<>();
		if (tagIds.size() > properties.getMaxTagsPerPost()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_TOO_MANY_TAGS", "Too many tags.");
		}
		Set<String> unique = new LinkedHashSet<>(tagIds);
		if (unique.size() != tagIds.size()) throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_DUPLICATE_TAG", "Duplicate tags are not allowed.");
		List<BlogTag> tags = tagRepository.findByIdIn(unique);
		if (tags.size() != unique.size()) throw new ApiException(HttpStatus.NOT_FOUND, "BLOG_TAG_NOT_FOUND", "One or more blog tags were not found.");
		return new LinkedHashSet<>(tags);
	}

	private void publish(BlogPost post) {
		validatePublish(post);
		post.setStatus(BlogStatus.PUBLISHED);
		if (post.getPublishedDate() == null) post.setPublishedDate(Instant.now());
		post.setArchivedAt(null);
		post.setArchivedBy(null);
	}

	private void archive(BlogPost post, AdminPrincipal admin) {
		post.setStatus(BlogStatus.ARCHIVED);
		post.setArchivedAt(Instant.now());
		post.setArchivedBy(admin.id());
	}

	private void validatePublish(BlogPost post) {
		Map<String, String> errors = new LinkedHashMap<>();
		required(errors, "title", post.getTitle());
		if (!slugService.isValidSlug(post.getSlug())) errors.put("slug", "Slug must be lowercase URL friendly text.");
		required(errors, "excerpt", post.getExcerpt());
		required(errors, "metaTitle", post.getMetaTitle());
		required(errors, "metaDescription", post.getMetaDescription());
		required(errors, "authorName", post.getAuthorName());
		if (post.getCategory() == null) errors.put("categoryId", "Category is required.");
		else if (post.getCategory().getStatus() != CategoryStatus.ACTIVE) errors.put("categoryId", "Category must be active.");
		if (post.getBody().isEmpty() || !contentValidator.containsMeaningfulText(post.getBody())) errors.put("body", "Body must contain meaningful content.");
		if (properties.isCoverImageRequiredForPublish()) required(errors, "coverImageUrl", post.getCoverImageUrl());
		if (post.getCoverImageUrl() != null && !post.getCoverImageUrl().isBlank()) required(errors, "coverImageAlt", post.getCoverImageAlt());
		if (!errors.isEmpty()) {
			throw new BlogPublishValidationException(errors);
		}
	}

	private void required(Map<String, String> errors, String field, String value) {
		if (value == null || value.isBlank()) errors.put(field, field + " is required.");
	}

	private boolean allowed(BlogStatus from, BlogStatus to) {
		return switch (from) {
			case DRAFT -> to == BlogStatus.PUBLISHED || to == BlogStatus.ARCHIVED;
			case PUBLISHED -> to == BlogStatus.UNPUBLISHED || to == BlogStatus.ARCHIVED;
			case UNPUBLISHED -> to == BlogStatus.PUBLISHED || to == BlogStatus.ARCHIVED;
			case ARCHIVED -> to == BlogStatus.DRAFT;
		};
	}

	private void auditStatus(AdminPrincipal admin, BlogPost post, BlogStatus status) {
		AdminActionType action = switch (status) {
			case PUBLISHED -> AdminActionType.BLOG_PUBLISHED;
			case UNPUBLISHED -> AdminActionType.BLOG_UNPUBLISHED;
			case ARCHIVED -> AdminActionType.BLOG_ARCHIVED;
			case DRAFT -> AdminActionType.BLOG_RESTORED;
		};
		auditService.log(admin, action, post.getId(), "Blog post status changed to " + status + ": " + post.getSlug());
	}

	private BlogPost findDetailed(String id) {
		return postRepository.findDetailedById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BLOG_POST_NOT_FOUND", "Blog post not found."));
	}

	private String normalizeManualSlug(String slug) {
		String normalized = slugService.normalize(slug);
		if (!slugService.isValidSlug(normalized)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_INVALID_SLUG", "Blog slug is invalid.");
		}
		return normalized;
	}

	private boolean slugTaken(String slug) {
		return postRepository.existsBySlug(slug) || slugHistoryRepository.existsByOldSlug(slug);
	}

	private void revalidateServiceFor(BlogPost post, String oldSlug) {
		List<String> paths = new ArrayList<>();
		paths.add("/blog");
		paths.add("/blog/" + post.getSlug());
		if (oldSlug != null) paths.add("/blog/" + oldSlug);
		if (post.getCategory() != null) paths.add("/blog/category/" + post.getCategory().getSlug());
		paths.add("/sitemap.xml");
		revalidationService.afterCommit(paths);
	}

	private <T> PageResponse<T> page(Page<T> page) {
		return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(),
				page.getTotalPages(), page.isFirst(), page.isLast());
	}

	private ApiException conflict() {
		return new ApiException(HttpStatus.CONFLICT, "BLOG_VERSION_CONFLICT", "This blog post was updated by another user. Refresh and try again.");
	}

	private String trim(String value) {
		return value == null ? null : value.trim();
	}
}
