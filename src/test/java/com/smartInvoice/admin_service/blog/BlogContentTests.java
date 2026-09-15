package com.smartInvoice.admin_service.blog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartInvoice.admin_service.dto.BlogDtos.BlogBlockRequest;
import com.smartInvoice.admin_service.dto.BlogDtos.ProductLinkRequest;
import com.smartInvoice.admin_service.service.BlogContentValidator;
import com.smartInvoice.admin_service.service.BlogProperties;
import com.smartInvoice.admin_service.service.BlogReadTimeCalculator;
import com.smartInvoice.admin_service.web.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BlogContentTests {
	private final BlogProperties properties = new BlogProperties();
	private final BlogContentValidator validator = new BlogContentValidator(new ObjectMapper(), properties);
	private final BlogReadTimeCalculator calculator = new BlogReadTimeCalculator(new ObjectMapper());

	@Test
	void rejectsUnsupportedBlocksAndUnsafeHtml() {
		assertThrows(ApiException.class, () -> validator.toBlocks(List.of(new BlogBlockRequest("video", 0, Map.of()))));
		assertThrows(ApiException.class, () -> validator.toBlocks(List.of(
				new BlogBlockRequest("paragraph", 0, Map.of("text", "<script>alert(1)</script>")))));
	}

	@Test
	void validatesProductLinksAndCalculatesReadTime() {
		assertThrows(ApiException.class, () -> validator.toProductLinks(List.of(
				new ProductLinkRequest("Bad", "javascript:alert(1)", 0))));
		var blocks = validator.toBlocks(List.of(new BlogBlockRequest("paragraph", 0,
				Map.of("text", "This is a useful invoice article with enough meaningful words for validation."))));
		assertEquals(1, calculator.calculate(blocks));
		assertTrue(validator.containsMeaningfulText(blocks));
	}
}
