package com.smartInvoice.admin_service.web;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

	@ExceptionHandler(Exception.class)
	ResponseEntity<ApiError> fallback(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiError.of("INTERNAL_ERROR", "Unexpected admin service error."));
	}
}
