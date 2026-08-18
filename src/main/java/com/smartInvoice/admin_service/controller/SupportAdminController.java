package com.smartInvoice.admin_service.controller;

import com.smartInvoice.admin_service.domain.AdminNote;
import com.smartInvoice.admin_service.dto.Requests;
import com.smartInvoice.admin_service.dto.Responses.*;
import com.smartInvoice.admin_service.service.AdminPrincipal;
import com.smartInvoice.admin_service.service.SupportAdminService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/support/users")
public class SupportAdminController {
	private final SupportAdminService service;

	public SupportAdminController(SupportAdminService service) {
		this.service = service;
	}

	@GetMapping("/{userId}/records")
	public SupportRecords records(@PathVariable String userId) {
		return service.records(userId);
	}

	@PostMapping("/{userId}/password-reset")
	public PasswordResetResponse passwordReset(@PathVariable String userId, @AuthenticationPrincipal AdminPrincipal admin) {
		return service.triggerPasswordReset(userId, admin);
	}

	@PostMapping("/{userId}/notes")
	public AdminNote addNote(@PathVariable String userId, @Valid @RequestBody Requests.AddNoteRequest request,
			@AuthenticationPrincipal AdminPrincipal admin) {
		return service.addNote(userId, request, admin);
	}
}
