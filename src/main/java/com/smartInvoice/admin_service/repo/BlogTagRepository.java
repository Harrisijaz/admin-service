package com.smartInvoice.admin_service.repo;

import com.smartInvoice.admin_service.domain.BlogTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BlogTagRepository extends JpaRepository<BlogTag, String> {
	Optional<BlogTag> findBySlug(String slug);
	boolean existsBySlug(String slug);
	List<BlogTag> findByIdIn(Collection<String> ids);
}
