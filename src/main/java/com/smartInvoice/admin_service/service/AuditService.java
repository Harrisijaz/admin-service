package com.smartInvoice.admin_service.service;

import com.smartInvoice.admin_service.domain.AdminActionType;
import com.smartInvoice.admin_service.domain.AdminActivityLog;
import com.smartInvoice.admin_service.repo.AdminActivityLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
	private final AdminActivityLogRepository repository;

	public AuditService(AdminActivityLogRepository repository) {
		this.repository = repository;
	}

	public void log(AdminPrincipal admin, AdminActionType type, String targetUserId, String description) {
		AdminActivityLog log = new AdminActivityLog();
		log.setAdminId(admin.id());
		log.setActionType(type);
		log.setTargetUserId(targetUserId);
		log.setDescription(description);
		repository.save(log);
	}
}
