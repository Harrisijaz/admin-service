package com.smartInvoice.admin_service.web;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
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
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

	@ExceptionHandler(DataAccessException.class)
	ResponseEntity<ApiError> database(DataAccessException ex) {
		log.error("Database access failed", ex);
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(ApiError.of("DATABASE_UNAVAILABLE", "Admin service database is unavailable. Check the database connection."));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ApiError> fallback(Exception ex) {
		log.error("Unhandled admin service exception", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiError.of("INTERNAL_ERROR", "Admin service failed unexpectedly. Check server logs for the root cause."));
	}
}
