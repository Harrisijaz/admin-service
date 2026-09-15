package com.smartInvoice.admin_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class BlogRevalidationService {
	private static final Logger log = LoggerFactory.getLogger(BlogRevalidationService.class);
	private final BlogProperties properties;
	private final WebClient webClient;

	public BlogRevalidationService(BlogProperties properties, WebClient.Builder builder) {
		this.properties = properties;
		this.webClient = builder.build();
	}

	public void afterCommit(Collection<String> paths) {
		List<String> deduped = paths.stream().filter(Objects::nonNull).filter(path -> !path.isBlank()).distinct().toList();
		if (deduped.isEmpty()) return;
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					revalidateAsync(deduped);
				}
			});
		} else {
			revalidateAsync(deduped);
		}
	}

	@Async
	public void revalidateAsync(Collection<String> paths) {
		String url = properties.getNextjsRevalidationUrl();
		String secret = properties.getNextjsRevalidationSecret();
		if (url == null || url.isBlank()) {
			log.info("Next.js revalidation skipped because no URL is configured paths={}", paths);
			return;
		}
		try {
			webClient.post()
					.uri(url)
					.contentType(MediaType.APPLICATION_JSON)
					.header("X-Revalidation-Secret", secret == null ? "" : secret)
					.bodyValue(Map.of("paths", paths))
					.retrieve()
					.toBodilessEntity()
					.block();
			log.info("Next.js revalidation requested paths={}", paths);
		} catch (Exception ex) {
			log.warn("Next.js revalidation failed paths={} message={}", paths, ex.getMessage());
		}
	}
}
