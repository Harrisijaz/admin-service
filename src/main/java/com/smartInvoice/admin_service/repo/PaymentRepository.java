package com.smartInvoice.admin_service.repo;

import com.smartInvoice.admin_service.domain.Payment;
import com.smartInvoice.admin_service.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, String> {
	List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);
	List<Payment> findByStatusInOrderByCreatedAtDesc(Collection<PaymentStatus> statuses);

	@Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = 'SUCCEEDED' and p.createdAt >= :start and p.createdAt < :end")
	BigDecimal recognizedRevenue(@Param("start") Instant start, @Param("end") Instant end);
}
