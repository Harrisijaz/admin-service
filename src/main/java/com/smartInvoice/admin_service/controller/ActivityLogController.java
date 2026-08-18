package com.smartInvoice.admin_service.controller;

import com.smartInvoice.admin_service.domain.AdminActivityLog;
import com.smartInvoice.admin_service.repo.AdminActivityLogRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/activity-logs")
public class ActivityLogController {
	private final AdminActivityLogRepository repository;

	public ActivityLogController(AdminActivityLogRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	public List<AdminActivityLog> recent() {
		return repository.findTop200ByOrderByCreatedAtDesc();
	}
}
