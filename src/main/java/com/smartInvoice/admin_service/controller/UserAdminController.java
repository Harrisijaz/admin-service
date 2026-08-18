package com.smartInvoice.admin_service.controller;

import com.smartInvoice.admin_service.domain.AccountStatus;
import com.smartInvoice.admin_service.domain.PlanType;
import com.smartInvoice.admin_service.dto.Requests;
import com.smartInvoice.admin_service.dto.Responses.*;
import com.smartInvoice.admin_service.service.AdminPrincipal;
import com.smartInvoice.admin_service.service.UserAdminService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/admin/users")
public class UserAdminController {
	private final UserAdminService service;

	public UserAdminController(UserAdminService service) {
		this.service = service;
	}

	@GetMapping
	public UsersPage list(@RequestParam(required = false) String search,
			@RequestParam(required = false) PlanType plan,
			@RequestParam(required = false) AccountStatus status,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer limit,
			@RequestParam(required = false) String sortBy,
			@RequestParam(required = false) String direction) {
		return service.list(search, plan, status, startDate, endDate, page, limit, sortBy, direction);
	}

	@GetMapping("/export")
	public CsvExport export(@RequestParam(required = false) String search,
			@RequestParam(required = false) PlanType plan,
			@RequestParam(required = false) AccountStatus status,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
			@RequestParam(required = false) String sortBy,
			@RequestParam(required = false) String direction) {
		return service.exportCsv(search, plan, status, startDate, endDate, sortBy, direction);
	}

	@GetMapping("/{userId}")
	public UserDetail detail(@PathVariable String userId) {
		return service.detail(userId);
	}

	@PostMapping("/{userId}/block")
	public MessageResponse block(@PathVariable String userId, @Valid @RequestBody Requests.BlockUserRequest request,
			@AuthenticationPrincipal AdminPrincipal admin) {
		return service.block(userId, request, admin);
	}

	@PostMapping("/{userId}/unblock")
	public MessageResponse unblock(@PathVariable String userId, @AuthenticationPrincipal AdminPrincipal admin) {
		return service.unblock(userId, admin);
	}

	@DeleteMapping("/{userId}")
	public MessageResponse delete(@PathVariable String userId, @Valid @RequestBody Requests.DeleteUserRequest request,
			@AuthenticationPrincipal AdminPrincipal admin) {
		return service.delete(userId, request, admin);
	}
}
