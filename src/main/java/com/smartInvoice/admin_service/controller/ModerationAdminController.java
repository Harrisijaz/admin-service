package com.smartInvoice.admin_service.controller;

import com.smartInvoice.admin_service.domain.ModerationFlag;
import com.smartInvoice.admin_service.dto.Requests;
import com.smartInvoice.admin_service.dto.Responses.*;
import com.smartInvoice.admin_service.service.AdminPrincipal;
import com.smartInvoice.admin_service.service.ModerationAdminService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/moderation")
public class ModerationAdminController {
	private final ModerationAdminService service;

	public ModerationAdminController(ModerationAdminService service) {
		this.service = service;
	}

	@GetMapping("/flags")
	public ModerationOverview flags() {
		return service.openFlags();
	}

	@PostMapping("/flags/{flagId}/dismiss")
	public ModerationFlag dismiss(@PathVariable String flagId, @Valid @RequestBody Requests.DismissFlagRequest request,
			@AuthenticationPrincipal AdminPrincipal admin) {
		return service.dismiss(flagId, request, admin);
	}

	@PostMapping("/users/{userId}/trusted")
	public MessageResponse trusted(@PathVariable String userId, @AuthenticationPrincipal AdminPrincipal admin) {
		return service.markTrusted(userId, admin);
	}
}
