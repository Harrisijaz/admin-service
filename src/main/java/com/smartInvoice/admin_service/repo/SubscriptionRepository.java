package com.smartInvoice.admin_service.repo;

import com.smartInvoice.admin_service.domain.PlanType;
import com.smartInvoice.admin_service.domain.Subscription;
import com.smartInvoice.admin_service.domain.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
	Optional<Subscription> findFirstByUserIdOrderByStartDateDesc(String userId);
	List<Subscription> findByUserIdOrderByStartDateDesc(String userId);
	long countByPlanTypeAndStatusIn(PlanType planType, Collection<SubscriptionStatus> statuses);
	long countByPlanTypeAndCancelledAtBetween(PlanType planType, Instant start, Instant end);
	boolean existsByUserIdAndPlanTypeAndStatusIn(String userId, PlanType planType, Collection<SubscriptionStatus> statuses);
}
