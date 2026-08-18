package com.smartInvoice.admin_service.controller;

import com.smartInvoice.admin_service.dto.Responses.*;
import com.smartInvoice.admin_service.service.AnalyticsAdminService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/analytics")
public class AnalyticsAdminController {
	private final AnalyticsAdminService service;

	public AnalyticsAdminController(AnalyticsAdminService service) {
		this.service = service;
	}

	@GetMapping("/dashboard")
	public DashboardResponse dashboard(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		return service.dashboard(startDate, endDate);
	}

	@GetMapping("/ai-usage")
	public AiUsageOverview aiUsage(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate billingCycleStart) {
		return service.aiUsage(billingCycleStart);
	}
}
