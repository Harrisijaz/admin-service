package com.smartInvoice.admin_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartInvoice.admin_service.domain.BlogContentBlock;
import com.smartInvoice.admin_service.domain.BlogProductLink;
import com.smartInvoice.admin_service.dto.BlogDtos.BlogBlockRequest;
import com.smartInvoice.admin_service.dto.BlogDtos.ProductLinkRequest;
import com.smartInvoice.admin_service.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class BlogContentValidator {
	private static final Set<String> TYPES = Set.of("paragraph", "heading", "image", "bulletList", "numberedList",
			"quote", "code", "table", "callout", "productCTA");
	private static final Pattern SCRIPT = Pattern.compile("(?is)<\\s*script|javascript:|on\\w+\\s*=");
	private final ObjectMapper objectMapper;
	private final BlogProperties properties;

	public BlogContentValidator(ObjectMapper objectMapper, BlogProperties properties) {
		this.objectMapper = objectMapper;
		this.properties = properties;
	}

	public List<BlogContentBlock> toBlocks(List<BlogBlockRequest> requests) {
		if (requests == null) return List.of();
		List<BlogContentBlock> blocks = new ArrayList<>();
		Set<Integer> orders = new HashSet<>();
		for (BlogBlockRequest request : requests) {
			if (!TYPES.contains(request.type())) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_INVALID_BLOCK", "Unsupported blog block type: " + request.type());
			}
			if (!orders.add(request.order())) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_INVALID_BLOCK", "Duplicate block order: " + request.order());
			}
			validateBlockData(request);
			BlogContentBlock block = new BlogContentBlock();
			block.setType(request.type());
			block.setBlockOrder(request.order());
			block.setDataJson(toJson(request.data()));
			blocks.add(block);
		}
		blocks.sort(Comparator.comparingInt(BlogContentBlock::getBlockOrder));
		return blocks;
	}

	public List<BlogProductLink> toProductLinks(List<ProductLinkRequest> requests) {
		if (requests == null) return List.of();
		if (requests.size() > properties.getMaxProductLinksPerPost()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_TOO_MANY_PRODUCT_LINKS", "Too many product links.");
		}
		Set<String> hrefs = new HashSet<>();
		List<BlogProductLink> links = new ArrayList<>();
		for (ProductLinkRequest request : requests) {
			String href = trim(request.href());
			if (!isSafeHref(href)) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_INVALID_PRODUCT_LINK", "Product link href is invalid.");
			}
			if (!hrefs.add(href.toLowerCase(Locale.ROOT))) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_DUPLICATE_PRODUCT_LINK", "Duplicate product link href.");
			}
			BlogProductLink link = new BlogProductLink();
			link.setLabel(trim(request.label()));
			link.setHref(href);
			link.setLinkOrder(request.order());
			links.add(link);
		}
		links.sort(Comparator.comparingInt(BlogProductLink::getLinkOrder));
		return links;
	}

	public boolean containsMeaningfulText(List<BlogContentBlock> blocks) {
		return blocks.stream().anyMatch(block -> block.getDataJson() != null && block.getDataJson().replaceAll("[^A-Za-z0-9]", "").length() >= 20);
	}

	private void validateBlockData(BlogBlockRequest request) {
		String json = toJson(request.data());
		if (SCRIPT.matcher(json).find()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_UNSAFE_CONTENT", "Blog content contains unsafe HTML or URLs.");
		}
		if ("heading".equals(request.type())) {
			Object level = request.data().get("level");
			if (!(level instanceof Number number) || number.intValue() < 2 || number.intValue() > 4) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_INVALID_HEADING", "Heading level must be between 2 and 4.");
			}
		}
		if ("image".equals(request.type()) && !isSafeHref(String.valueOf(request.data().getOrDefault("url", "")))) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_INVALID_IMAGE", "Image block URL is invalid.");
		}
	}

	private String toJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException ex) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_INVALID_BLOCK", "Block data must be valid JSON.");
		}
	}

	private boolean isSafeHref(String href) {
		if (href == null || href.isBlank()) return false;
		String lower = href.toLowerCase(Locale.ROOT).trim();
		if (lower.startsWith("javascript:") || lower.startsWith("data:") || lower.startsWith("file:")) return false;
		return lower.startsWith("/") || lower.startsWith("https://") || lower.startsWith("http://");
	}

	private String trim(String value) {
		return value == null ? null : value.trim();
	}
}
