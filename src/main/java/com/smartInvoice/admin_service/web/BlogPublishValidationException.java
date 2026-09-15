package com.smartInvoice.admin_service.web;

import java.util.Map;

public class BlogPublishValidationException extends RuntimeException {
	private final Map<String, String> errors;

	public BlogPublishValidationException(Map<String, String> errors) {
		super("Blog post cannot be published.");
		this.errors = Map.copyOf(errors);
	}

	public Map<String, String> getErrors() {
		return errors;
	}
}
