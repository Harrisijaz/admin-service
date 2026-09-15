package com.smartInvoice.admin_service.blog;

import com.smartInvoice.admin_service.domain.BlogStatus;
import com.smartInvoice.admin_service.dto.BlogDtos.*;
import com.smartInvoice.admin_service.service.BlogCategoryService;
import com.smartInvoice.admin_service.service.BlogPostService;
import com.smartInvoice.admin_service.service.AdminPrincipal;
import com.smartInvoice.admin_service.web.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BlogPostIntegrationTests {
	private final AdminPrincipal admin = new AdminPrincipal("admin_test", "admin@example.com", "jti");

	@Autowired
	BlogCategoryService categoryService;
	@Autowired
	BlogPostService postService;

	@Test
	void draftIsNotPublicUntilPublishedThenArchiveRemovesIt() {
		CategoryResponse category = categoryService.create(new CreateBlogCategoryRequest("Invoicing", "", "Invoice guides", null), admin);
		BlogPostAdminResponse draft = postService.create(new CreateBlogPostRequest(
				"How to Create an Invoice",
				"",
				"Learn how to create professional invoices for clients with clear payment details.",
				"How to Create an Invoice",
				"Learn how to create a professional invoice for your business clients.",
				"https://cdn.example.com/invoice.webp",
				"Professional invoice example",
				"Invorights Team",
				category.id(),
				List.of(),
				List.of(new BlogBlockRequest("heading", 0, Map.of("level", 2, "text", "Create better invoices")),
						new BlogBlockRequest("paragraph", 1, Map.of("text",
								"Professional invoices should include client details, line items, taxes, totals, and clear payment terms for faster payment collection."))),
				List.of(new ProductLinkRequest("Create Invoice Free", "/invoice-generator", 0)),
				BlogStatus.DRAFT), admin);

		assertThrows(ApiException.class, () -> postService.publicDetail(draft.slug()));

		BlogPostAdminResponse published = postService.changeStatus(draft.id(),
				new ChangeBlogPostStatusRequest(BlogStatus.PUBLISHED), admin);
		assertNotNull(published.publishedDate());
		assertEquals(published.slug(), postService.publicDetail(published.slug()).slug());

		postService.archive(published.id(), admin);
		assertThrows(ApiException.class, () -> postService.publicDetail(published.slug()));
	}

	@Test
	void publishValidationReturnsFieldErrors() {
		BlogPostAdminResponse draft = postService.create(new CreateBlogPostRequest(
				"Incomplete Blog Post", "", null, null, null, null, null, null, null, List.of(), List.of(),
				List.of(), BlogStatus.DRAFT), admin);

		ApiException ex = assertThrows(ApiException.class, () -> postService.publicDetail(draft.slug()));
		assertEquals("BLOG_POST_NOT_FOUND", ex.getCode());
		assertThrows(RuntimeException.class, () -> postService.changeStatus(draft.id(),
				new ChangeBlogPostStatusRequest(BlogStatus.PUBLISHED), admin));
	}
}
