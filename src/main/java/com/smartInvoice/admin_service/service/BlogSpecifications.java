package com.smartInvoice.admin_service.service;

import com.smartInvoice.admin_service.domain.BlogPost;
import com.smartInvoice.admin_service.domain.BlogStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class BlogSpecifications {
	private BlogSpecifications() {
	}

	public static Specification<BlogPost> status(BlogStatus status) {
		return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
	}

	public static Specification<BlogPost> categorySlug(String slug) {
		return (root, query, cb) -> blank(slug) ? cb.conjunction() : cb.equal(root.join("category", JoinType.LEFT).get("slug"), slug);
	}

	public static Specification<BlogPost> categoryId(String id) {
		return (root, query, cb) -> blank(id) ? cb.conjunction() : cb.equal(root.join("category", JoinType.LEFT).get("id"), id);
	}

	public static Specification<BlogPost> tagSlug(String slug) {
		return (root, query, cb) -> {
			if (blank(slug)) return cb.conjunction();
			query.distinct(true);
			return cb.equal(root.joinSet("tags", JoinType.LEFT).get("slug"), slug);
		};
	}

	public static Specification<BlogPost> tagId(String id) {
		return (root, query, cb) -> {
			if (blank(id)) return cb.conjunction();
			query.distinct(true);
			return cb.equal(root.joinSet("tags", JoinType.LEFT).get("id"), id);
		};
	}

	public static Specification<BlogPost> search(String search) {
		return (root, query, cb) -> {
			if (blank(search)) return cb.conjunction();
			query.distinct(true);
			String like = "%" + search.trim().toLowerCase() + "%";
			return cb.or(
					cb.like(cb.lower(root.get("title")), like),
					cb.like(cb.lower(root.get("excerpt")), like),
					cb.like(cb.lower(root.join("category", JoinType.LEFT).get("name")), like),
					cb.like(cb.lower(root.joinSet("tags", JoinType.LEFT).get("name")), like)
			);
		};
	}

	public static Specification<BlogPost> createdBy(String createdBy) {
		return (root, query, cb) -> blank(createdBy) ? cb.conjunction() : cb.equal(root.get("createdBy"), createdBy);
	}

	public static Specification<BlogPost> publishedBetween(Instant from, Instant to) {
		return (root, query, cb) -> {
			if (from == null && to == null) return cb.conjunction();
			if (from != null && to != null) return cb.between(root.get("publishedDate"), from, to);
			if (from != null) return cb.greaterThanOrEqualTo(root.get("publishedDate"), from);
			return cb.lessThanOrEqualTo(root.get("publishedDate"), to);
		};
	}

	private static boolean blank(String value) {
		return value == null || value.isBlank();
	}
}
