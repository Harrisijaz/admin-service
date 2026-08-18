package com.smartInvoice.admin_service.service;

import com.smartInvoice.admin_service.domain.*;
import com.smartInvoice.admin_service.dto.Requests;
import com.smartInvoice.admin_service.dto.Responses.*;
import com.smartInvoice.admin_service.repo.*;
import com.smartInvoice.admin_service.web.ApiException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class UserAdminService {
	private static final Set<String> SORTS = Set.of("createdAt", "status");
	private final UserRepository users;
	private final SubscriptionRepository subscriptions;
	private final PaymentRepository payments;
	private final AdminNoteRepository notes;
	private final ModerationFlagRepository flags;
	private final AuditService audit;
	private final IntegrationClient integrations;
	private final Clock clock;

	public UserAdminService(UserRepository users, SubscriptionRepository subscriptions, PaymentRepository payments,
			AdminNoteRepository notes, ModerationFlagRepository flags, AuditService audit, IntegrationClient integrations, Clock clock) {
		this.users = users;
		this.subscriptions = subscriptions;
		this.payments = payments;
		this.notes = notes;
		this.flags = flags;
		this.audit = audit;
		this.integrations = integrations;
		this.clock = clock;
	}

	public UsersPage list(String search, PlanType plan, AccountStatus status, Instant startDate, Instant endDate,
			Integer page, Integer limit, String sortBy, String direction) {
		AdminValidation.validateSearch(search);
		AdminValidation.validateInstantRange(startDate, endDate, Duration.ofDays(365 * 2L));
		Pageable pageable = pageable(page, limit, sortBy, direction);
		Page<UserSummary> result = users.findAll((root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			String clean = AdminValidation.trimmed(search);
			if (clean != null && !clean.isBlank()) {
				String like = "%" + clean.toLowerCase() + "%";
				predicates.add(cb.or(cb.like(cb.lower(root.get("fullName")), like), cb.like(cb.lower(root.get("emailNormalized")), like)));
			}
			if (status != null) predicates.add(cb.equal(root.get("status"), status));
			if (startDate != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
			if (endDate != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
			return cb.and(predicates.toArray(Predicate[]::new));
		}, pageable).map(this::summary);
		if (plan != null) {
			List<UserSummary> filtered = result.getContent().stream().filter(u -> u.planType() == plan).toList();
			result = new PageImpl<>(filtered, pageable, filtered.size());
		}
		return new UsersPage(result, result.isEmpty() ? "No users found matching your search" : null);
	}

	public UserDetail detail(String userId) {
		User user = user(userId);
		return new UserDetail(summary(user),
				subscriptions.findByUserIdOrderByStartDateDesc(userId),
				payments.findByUserIdOrderByCreatedAtDesc(userId),
				notes.findByUserIdOrderByCreatedAtDesc(userId),
				integrations.invoices(userId),
				integrations.expenses(userId));
	}

	@Transactional
	public MessageResponse block(String userId, Requests.BlockUserRequest request, AdminPrincipal admin) {
		User user = user(userId);
		if (user.getStatus() == AccountStatus.BLOCKED) {
			return new MessageResponse("ALREADY_BLOCKED", "User is already blocked.");
		}
		if (user.getStatus() == AccountStatus.DELETED) {
			throw new ApiException(HttpStatus.NOT_FOUND, "USER_NO_LONGER_EXISTS", "User no longer exists.");
		}
		user.setStatus(AccountStatus.BLOCKED);
		audit.log(admin, AdminActionType.BLOCK_USER, userId, "Blocked user. Reason: " + request.reason().trim());
		integrations.invalidateUserSessions(userId);
		return new MessageResponse("USER_BLOCKED", "User blocked.");
	}

	@Transactional
	public MessageResponse unblock(String userId, AdminPrincipal admin) {
		User user = user(userId);
		if (user.getStatus() == AccountStatus.DELETED) {
			throw new ApiException(HttpStatus.NOT_FOUND, "USER_NO_LONGER_EXISTS", "User no longer exists.");
		}
		if (user.getStatus() == AccountStatus.ACTIVE) {
			return new MessageResponse("ALREADY_ACTIVE", "User is already active.");
		}
		user.setStatus(AccountStatus.ACTIVE);
		audit.log(admin, AdminActionType.UNBLOCK_USER, userId, "Unblocked user.");
		return new MessageResponse("USER_UNBLOCKED", "User unblocked.");
	}

	@Transactional
	public MessageResponse delete(String userId, Requests.DeleteUserRequest request, AdminPrincipal admin) {
		User user = user(userId);
		if (user.getStatus() == AccountStatus.DELETED) {
			throw new ApiException(HttpStatus.NOT_FOUND, "USER_NO_LONGER_EXISTS", "User no longer exists.");
		}
		if (!user.getEmail().equalsIgnoreCase(request.confirmationEmail().trim())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "DELETE_CONFIRMATION_MISMATCH", "Delete confirmation must match the user's email address.");
		}
		if (flags.existsByUserIdAndStatus(userId, FlagStatus.OPEN)) {
			throw new ApiException(HttpStatus.CONFLICT, "ACTIVE_DISPUTE_OR_REVIEW", "Cannot delete a user with an active unresolved dispute/refund case.");
		}
		boolean activePaid = subscriptions.existsByUserIdAndPlanTypeAndStatusIn(userId, PlanType.PAID, List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PENDING, SubscriptionStatus.PAST_DUE));
		if (activePaid && !request.cancelSubscriptionFirst()) {
			throw new ApiException(HttpStatus.CONFLICT, "ACTIVE_SUBSCRIPTION", "This user has an active subscription. Cancel it first.");
		}
		if (activePaid) {
			integrations.cancelSubscription(userId);
			subscriptions.findFirstByUserIdOrderByStartDateDesc(userId).ifPresent(s -> {
				s.setStatus(SubscriptionStatus.CANCELLED);
				s.setCancelledAt(Instant.now(clock));
			});
			audit.log(admin, AdminActionType.CANCEL_SUBSCRIPTION, userId, "Cancelled active subscription before delete.");
		}
		user.setStatus(AccountStatus.DELETED);
		user.setDeletedAt(Instant.now(clock));
		audit.log(admin, AdminActionType.DELETE_USER, userId, "Soft-deleted user.");
		integrations.invalidateUserSessions(userId);
		return new MessageResponse("USER_DELETED", "User soft-deleted.");
	}

	public CsvExport exportCsv(String search, PlanType plan, AccountStatus status, Instant startDate, Instant endDate, String sortBy, String direction) {
		UsersPage page = list(search, plan, status, startDate, endDate, 1, 100, sortBy, direction);
		StringBuilder csv = new StringBuilder("id,name,email,signupDate,planType,accountStatus\n");
		page.users().getContent().forEach(u -> csv.append(escape(u.id())).append(',').append(escape(u.name())).append(',')
				.append(escape(u.email())).append(',').append(u.signupDate()).append(',').append(u.planType()).append(',').append(u.accountStatus()).append('\n'));
		return new CsvExport("smartinvoice-users.csv", "text/csv", csv.toString());
	}

	private Pageable pageable(Integer page, Integer limit, String sortBy, String direction) {
		int p = page == null || page < 1 ? 0 : page - 1;
		int l = limit == null || limit < 10 || limit > 100 ? 20 : limit;
		String sort = SORTS.contains(sortBy) ? sortBy : "createdAt";
		Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
		return PageRequest.of(p, l, Sort.by(dir, sort));
	}

	private UserSummary summary(User user) {
		PlanType plan = subscriptions.findFirstByUserIdOrderByStartDateDesc(user.getId()).map(Subscription::getPlanType).orElse(PlanType.FREE);
		return new UserSummary(user.getId(), user.getFullName(), user.getEmail(), user.getCreatedAt(), plan, user.getStatus());
	}

	private User user(String userId) {
		return users.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User no longer exists."));
	}

	private String escape(String value) {
		String safe = value == null ? "" : value;
		return "\"" + safe.replace("\"", "\"\"") + "\"";
	}
}
