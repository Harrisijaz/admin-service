package com.smartInvoice.admin_service.repo;

import com.smartInvoice.admin_service.domain.AdminNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminNoteRepository extends JpaRepository<AdminNote, String> {
	List<AdminNote> findByUserIdOrderByCreatedAtDesc(String userId);
}
