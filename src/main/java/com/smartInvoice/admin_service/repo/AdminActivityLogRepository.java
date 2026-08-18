package com.smartInvoice.admin_service.repo;

import com.smartInvoice.admin_service.domain.AdminActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminActivityLogRepository extends JpaRepository<AdminActivityLog, String> {
	List<AdminActivityLog> findTop200ByOrderByCreatedAtDesc();
}
