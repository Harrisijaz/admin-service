package com.smartInvoice.admin_service.repo;

import com.smartInvoice.admin_service.domain.AiUsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AiUsageRecordRepository extends JpaRepository<AiUsageRecord, String> {
	Optional<AiUsageRecord> findByUserIdAndBillingCycleStart(String userId, LocalDate billingCycleStart);
	List<AiUsageRecord> findByBillingCycleStartOrderByCountDesc(LocalDate billingCycleStart);

	@Query("select coalesce(sum(a.estimatedCost), 0) from AiUsageRecord a where a.billingCycleStart = :cycle")
	BigDecimal totalCostForCycle(@Param("cycle") LocalDate cycle);
}
