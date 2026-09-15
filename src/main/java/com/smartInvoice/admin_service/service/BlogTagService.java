package com.smartInvoice.admin_service.service;

import com.smartInvoice.admin_service.domain.BlogTag;
import com.smartInvoice.admin_service.dto.BlogDtos.TagResponse;
import com.smartInvoice.admin_service.repo.BlogTagRepository;
import com.smartInvoice.admin_service.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BlogTagService {
	private final BlogTagRepository repository;
	private final BlogSlugService slugService;

	public BlogTagService(BlogTagRepository repository, BlogSlugService slugService) {
		this.repository = repository;
		this.slugService = slugService;
	}

	@Transactional(readOnly = true)
	public List<TagResponse> list() {
		return repository.findAll().stream().map(this::response).toList();
	}

	@Transactional
	public TagResponse create(String name) {
		if (name == null || name.isBlank() || name.trim().length() > 80) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_INVALID_TAG", "Tag name is required and must be <= 80 characters.");
		}
		String slug = slugService.normalize(name);
		return repository.findBySlug(slug).map(this::response).orElseGet(() -> {
			BlogTag tag = new BlogTag();
			tag.setName(name.trim());
			tag.setSlug(slug);
			return response(repository.save(tag));
		});
	}

	private TagResponse response(BlogTag tag) {
		return new TagResponse(tag.getId(), tag.getName(), tag.getSlug());
	}
}
