package com.smartInvoice.admin_service.web;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(ApiException.class)
	ResponseEntity<ApiError> api(ApiException ex) {
		return ResponseEntity.status(ex.getStatus()).body(ApiError.of(ex.getCode(), ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiError> bodyValidation(MethodArgumentNotValidException ex) {
		Map<String, String> fields = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));
		return ResponseEntity.badRequest().body(new ApiError("VALIDATION_ERROR", "Request validation failed.", Instant.now(), fields));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<ApiError> paramValidation(ConstraintViolationException ex) {
		return ResponseEntity.badRequest().body(ApiError.of("VALIDATION_ERROR", ex.getMessage()));
	}

	@ExceptionHandler(BlogPublishValidationException.class)
	ResponseEntity<ApiError> blogPublishValidation(BlogPublishValidationException ex) {
		return ResponseEntity.badRequest()
				.body(new ApiError("BLOG_PUBLISH_VALIDATION_FAILED", ex.getMessage(), Instant.now(), ex.getErrors()));
	}

	@ExceptionHandler(OptimisticLockingFailureException.class)
	ResponseEntity<ApiError> optimisticLock(OptimisticLockingFailureException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(ApiError.of("BLOG_VERSION_CONFLICT", "This blog post was updated by another user. Refresh and try again."));
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	ResponseEntity<ApiError> maxUpload(MaxUploadSizeExceededException ex) {
		return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
				.body(ApiError.of("BLOG_UPLOAD_TOO_LARGE", "Uploaded file is too large."));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ApiError> fallback(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiError.of("INTERNAL_ERROR", "Unexpected admin service error."));
	}
}
