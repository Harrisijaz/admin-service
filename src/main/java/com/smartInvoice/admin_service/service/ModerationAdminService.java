package com.smartInvoice.admin_service.service;

import com.smartInvoice.admin_service.domain.*;
import com.smartInvoice.admin_service.dto.Requests;
import com.smartInvoice.admin_service.dto.Responses.*;
import com.smartInvoice.admin_service.repo.ModerationFlagRepository;
import com.smartInvoice.admin_service.repo.UserRepository;
import com.smartInvoice.admin_service.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

@Service
public class ModerationAdminService {
	private final ModerationFlagRepository flags;
	private final UserRepository users;
	private final AuditService audit;
	private final Clock clock;

	public ModerationAdminService(ModerationFlagRepository flags, UserRepository users, AuditService audit, Clock clock) {
		this.flags = flags;
		this.users = users;
		this.audit = audit;
		this.clock = clock;
	}

	public ModerationOverview openFlags() {
		return new ModerationOverview(flags.findByStatusOrderByCreatedAtDesc(FlagStatus.OPEN),
				Map.of("autoFlagging", "Auto-flagging never blocks users automatically in v1."));
	}

	@Transactional
	public ModerationFlag dismiss(String flagId, Requests.DismissFlagRequest request, AdminPrincipal admin) {
		ModerationFlag flag = flag(flagId);
		flag.setStatus(FlagStatus.DISMISSED);
		flag.setDismissalNote(request.note() == null ? null : request.note().trim());
		flag.setDismissedAt(Instant.now(clock));
		flag.setDismissedByAdminId(admin.id());
		audit.log(admin, AdminActionType.DISMISS_FLAG, flag.getUserId(), "Dismissed moderation flag " + flagId + ".");
		return flag;
	}

	@Transactional
	public MessageResponse markTrusted(String userId, AdminPrincipal admin) {
		User user = users.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User no longer exists."));
		user.setTrusted(true);
		audit.log(admin, AdminActionType.MARK_TRUSTED, userId, "Marked user trusted; auto-flagging should skip repeat noise.");
		return new MessageResponse("USER_TRUSTED", "User marked trusted.");
	}

	private ModerationFlag flag(String flagId) {
		return flags.findById(flagId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "FLAG_NOT_FOUND", "Moderation flag not found."));
	}
}
