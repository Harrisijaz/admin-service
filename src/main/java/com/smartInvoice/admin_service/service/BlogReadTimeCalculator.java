package com.smartInvoice.admin_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartInvoice.admin_service.domain.BlogContentBlock;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BlogReadTimeCalculator {
	private final ObjectMapper objectMapper;

	public BlogReadTimeCalculator(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public int calculate(List<BlogContentBlock> blocks) {
		int words = 0;
		for (BlogContentBlock block : blocks) {
			words += countWords(block.getType());
			try {
				words += countJsonText(objectMapper.readTree(block.getDataJson()));
			} catch (Exception ignored) {
				words += countWords(block.getDataJson());
			}
		}
		return Math.max(1, (int) Math.ceil(words / 200.0));
	}

	private int countJsonText(JsonNode node) {
		if (node == null || node.isNull()) return 0;
		if (node.isTextual()) return countWords(node.asText());
		int words = 0;
		if (node.isContainerNode()) {
			for (JsonNode child : node) words += countJsonText(child);
		}
		return words;
	}

	private int countWords(String text) {
		if (text == null || text.isBlank()) return 0;
		return text.trim().split("\\s+").length;
	}
}
