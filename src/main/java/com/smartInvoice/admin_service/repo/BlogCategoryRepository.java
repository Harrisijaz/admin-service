package com.smartInvoice.admin_service.repo;

import com.smartInvoice.admin_service.domain.BlogCategory;
import com.smartInvoice.admin_service.domain.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BlogCategoryRepository extends JpaRepository<BlogCategory, String> {
	Optional<BlogCategory> findBySlug(String slug);
	boolean existsBySlug(String slug);
	boolean existsBySlugAndIdNot(String slug, String id);
	boolean existsByNameIgnoreCase(String name);
	boolean existsByNameIgnoreCaseAndIdNot(String name, String id);

	@Query("""
			select c from BlogCategory c
			where c.status = :status and exists (
				select p.id from BlogPost p where p.category = c and p.status = com.smartInvoice.admin_service.domain.BlogStatus.PUBLISHED
			)
			order by c.name asc
			""")
	List<BlogCategory> findPublicCategories(CategoryStatus status);
}
