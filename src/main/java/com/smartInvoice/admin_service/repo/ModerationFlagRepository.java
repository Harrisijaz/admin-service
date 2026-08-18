package com.smartInvoice.admin_service.repo;

import com.smartInvoice.admin_service.domain.FlagStatus;
import com.smartInvoice.admin_service.domain.ModerationFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModerationFlagRepository extends JpaRepository<ModerationFlag, String> {
	List<ModerationFlag> findByStatusOrderByCreatedAtDesc(FlagStatus status);
	boolean existsByUserIdAndStatus(String userId, FlagStatus status);
}
