package com.smartInvoice.admin_service.repo;

import com.smartInvoice.admin_service.domain.AccountStatus;
import com.smartInvoice.admin_service.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
	long countByStatusNot(AccountStatus status);
	long countByStatusNotAndCreatedAtBetween(AccountStatus status, Instant start, Instant end);
	long countByStatus(AccountStatus status);
	Optional<User> findByEmailNormalized(String emailNormalized);
}
