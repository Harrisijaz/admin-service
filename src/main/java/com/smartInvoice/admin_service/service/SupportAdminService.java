package com.smartInvoice.admin_service.service;

import com.smartInvoice.admin_service.domain.*;
import com.smartInvoice.admin_service.dto.Requests;
import com.smartInvoice.admin_service.dto.Responses.*;
import com.smartInvoice.admin_service.repo.AdminNoteRepository;
import com.smartInvoice.admin_service.repo.UserRepository;
import com.smartInvoice.admin_service.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SupportAdminService {
	private final UserRepository users;
	private final AdminNoteRepository notes;
	private final IntegrationClient integrations;
	private final AuditService audit;
	private final Clock clock;
	private final Map<String, Instant> passwordResetLastSent = new ConcurrentHashMap<>();

	public SupportAdminService(UserRepository users, AdminNoteRepository notes, IntegrationClient integrations, AuditService audit, Clock clock) {
		this.users = users;
		this.notes = notes;
		this.integrations = integrations;
		this.audit = audit;
		this.clock = clock;
	}

	public SupportRecords records(String userId) {
		return new SupportRecords(integrations.invoices(userId), integrations.expenses(userId));
	}

	public AdminNote addNote(String userId, Requests.AddNoteRequest request, AdminPrincipal admin) {
		user(userId);
		AdminNote note = new AdminNote();
		note.setUserId(userId);
		note.setAdminId(admin.id());
		note.setNoteText(request.note().trim());
		AdminNote saved = notes.save(note);
		audit.log(admin, AdminActionType.ADD_NOTE, userId, "Added internal note.");
		return saved;
	}

	public PasswordResetResponse triggerPasswordReset(String userId, AdminPrincipal admin) {
		User user = user(userId);
		if (user.getStatus() == AccountStatus.DELETED) {
			throw new ApiException(HttpStatus.CONFLICT, "DELETED_USER_PASSWORD_RESET_DISABLED", "Cannot reset password for a deleted account.");
		}
		Instant now = Instant.now(clock);
		Instant last = passwordResetLastSent.get(userId);
		if (last != null && last.plus(Duration.ofMinutes(5)).isAfter(now)) {
			return new PasswordResetResponse("Password reset is rate-limited to 1 trigger per user per 5 minutes.", last.plus(Duration.ofMinutes(5)));
		}
		integrations.triggerPasswordReset(user.getEmail());
		passwordResetLastSent.put(userId, now);
		audit.log(admin, AdminActionType.TRIGGER_PASSWORD_RESET, userId, "Triggered password reset email.");
		return new PasswordResetResponse("Password reset email sent.", now.plus(Duration.ofMinutes(5)));
	}

	private User user(String userId) {
		return users.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User no longer exists."));
	}
}
