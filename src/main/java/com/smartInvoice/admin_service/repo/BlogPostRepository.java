package com.smartInvoice.admin_service.repo;

import com.smartInvoice.admin_service.domain.BlogPost;
import com.smartInvoice.admin_service.domain.BlogStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, String>, JpaSpecificationExecutor<BlogPost> {
	Optional<BlogPost> findBySlugAndStatus(String slug, BlogStatus status);

	@EntityGraph(attributePaths = {"category", "tags"})
	@Query("select p from BlogPost p where p.id = :id")
	Optional<BlogPost> findDetailedById(@Param("id") String id);

	@EntityGraph(attributePaths = {"category", "tags"})
	@Query("select p from BlogPost p where p.slug = :slug and p.status = :status")
	Optional<BlogPost> findDetailedBySlugAndStatus(@Param("slug") String slug, @Param("status") BlogStatus status);

	boolean existsBySlug(String slug);
	boolean existsBySlugAndIdNot(String slug, String id);
	long countByCategoryIdAndStatusNot(String categoryId, BlogStatus status);
	long countByCategoryIdAndStatus(String categoryId, BlogStatus status);

	@Query("select p from BlogPost p where p.status = com.smartInvoice.admin_service.domain.BlogStatus.PUBLISHED order by p.updatedAt desc")
	List<BlogPost> findPublishedForSitemap();
}
