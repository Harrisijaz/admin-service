package com.smartInvoice.admin_service.service;

import com.smartInvoice.admin_service.web.ApiException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class BlogPageSupport {
	private static final Set<String> SAFE_SORTS = Set.of("publishedDate", "updatedAt", "createdAt", "title");
	private final BlogProperties properties;

	public BlogPageSupport(BlogProperties properties) {
		this.properties = properties;
	}

	public Pageable pageable(Integer page, Integer size, String sort, String defaultSort) {
		int p = page == null ? 0 : page;
		int s = size == null ? 10 : size;
		if (p < 0 || s < 1) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_INVALID_PAGINATION", "Page must be >= 0 and size must be >= 1.");
		}
		if (s > properties.getMaxPageSize()) s = properties.getMaxPageSize();
		Sort parsed = parseSort(sort == null || sort.isBlank() ? defaultSort : sort);
		return PageRequest.of(p, s, parsed);
	}

	private Sort parseSort(String value) {
		String[] parts = value.split(",");
		String field = parts[0].trim();
		if (!SAFE_SORTS.contains(field)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_INVALID_SORT", "Unsupported sort field.");
		}
		Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())
				? Sort.Direction.ASC : Sort.Direction.DESC;
		return Sort.by(direction, field);
	}
}
