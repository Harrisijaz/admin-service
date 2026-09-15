package com.smartInvoice.admin_service.blog;

import com.smartInvoice.admin_service.service.BlogSlugService;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BlogSlugServiceTests {
	private final BlogSlugService service = new BlogSlugService();

	@Test
	void normalizesTitleToUrlSafeSlug() {
		assertEquals("how-to-create-a-professional-invoice",
				service.normalize("How to Create a Professional Invoice!"));
		assertTrue(service.isValidSlug("how-to-create-a-professional-invoice"));
		assertFalse(service.isValidSlug("-bad slug-"));
	}

	@Test
	void generatesUniqueSlugWithSuffix() {
		Set<String> taken = Set.of("invoice-guide", "invoice-guide-2");
		assertEquals("invoice-guide-3", service.uniqueSlug("Invoice Guide", taken::contains));
	}
}
