package com.smartInvoice.admin_service.repo;

import com.smartInvoice.admin_service.domain.BlogSlugHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogSlugHistoryRepository extends JpaRepository<BlogSlugHistory, String> {
	boolean existsByOldSlug(String oldSlug);
}
