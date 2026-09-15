package com.smartInvoice.admin_service.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;

@Service
public class BlogSlugService {
	public String normalize(String value) {
		if (value == null || value.isBlank()) return "";
		String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.toLowerCase(Locale.ROOT)
				.replaceAll("[^a-z0-9\\s-]", "")
				.trim()
				.replaceAll("[\\s_]+", "-")
				.replaceAll("-{2,}", "-")
				.replaceAll("^-|-$", "");
		return normalized.length() > 180 ? normalized.substring(0, 180).replaceAll("-+$", "") : normalized;
	}

	public String uniqueSlug(String preferred, Predicate<String> exists) {
		String base = normalize(preferred);
		if (base.isBlank()) throw new IllegalArgumentException("Slug source must not be blank.");
		if (!exists.test(base)) return base;
		int suffix = 2;
		while (true) {
			String tail = "-" + suffix;
			String candidate = base;
			if (candidate.length() + tail.length() > 180) {
				candidate = candidate.substring(0, 180 - tail.length()).replaceAll("-+$", "");
			}
			candidate = candidate + tail;
			if (!exists.test(candidate)) return candidate;
			suffix++;
		}
	}

	public boolean isValidSlug(String slug) {
		return slug != null && slug.length() <= 180 && slug.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$");
	}
}
