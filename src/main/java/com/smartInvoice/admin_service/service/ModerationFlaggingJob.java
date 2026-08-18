package com.smartInvoice.admin_service.service;

import com.smartInvoice.admin_service.config.AdminProperties;
import com.smartInvoice.admin_service.domain.ModerationFlag;
import com.smartInvoice.admin_service.repo.ModerationFlagRepository;
import com.smartInvoice.admin_service.repo.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class ModerationFlaggingJob {
	private final WebClient userClient;
	private final UserRepository users;
	private final ModerationFlagRepository flags;

	public ModerationFlaggingJob(WebClient.Builder builder, AdminProperties properties, UserRepository users, ModerationFlagRepository flags) {
		this.userClient = builder.clone().baseUrl(properties.getIntegrations().getUserServiceBaseUrl()).build();
		this.users = users;
		this.flags = flags;
	}

	@Scheduled(fixedDelayString = "${admin.moderation.scan-delay-ms:300000}")
	public void flagSuspiciousAccounts() {
		List<Map> suspicious = userClient.get().uri("/internal/admin/moderation/suspicious-accounts")
				.retrieve().bodyToFlux(Map.class).collectList().onErrorReturn(List.of()).block();
		if (suspicious == null) return;
		for (Map item : suspicious) {
			String userId = String.valueOf(item.get("userId"));
			String reason = String.valueOf(item.getOrDefault("reason", "Suspicious activity threshold exceeded."));
			users.findById(userId).filter(user -> !user.isTrusted()).ifPresent(user -> {
				if (!flags.existsByUserIdAndStatus(userId, com.smartInvoice.admin_service.domain.FlagStatus.OPEN)) {
					ModerationFlag flag = new ModerationFlag();
					flag.setUserId(userId);
					flag.setReason(reason.length() > 500 ? reason.substring(0, 500) : reason);
					flags.save(flag);
				}
			});
		}
	}
}
