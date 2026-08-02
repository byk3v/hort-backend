package com.kubuci.hort.shared.error;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

	private final ApiProblemFactory problems;

	public ApiExceptionHandler(ApiProblemFactory problems) {
		this.problems = problems;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException exception,
		HttpServletRequest request) {
		ProblemDetail problem = problems.create(HttpStatus.BAD_REQUEST, "validation_failed",
			"One or more request fields are invalid.", request);
		List<FieldViolation> fields = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
			.toList();
		problem.setProperty("fieldErrors", fields);
		return ResponseEntity.badRequest()
			.body(problem);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<ProblemDetail> constraintValidation(ConstraintViolationException exception,
		HttpServletRequest request) {
		ProblemDetail problem = problems.create(HttpStatus.BAD_REQUEST, "validation_failed",
			"One or more request values are invalid.", request);
		problem.setProperty("fieldErrors", exception.getConstraintViolations()
			.stream()
			.map(error -> new FieldViolation(error.getPropertyPath()
				.toString(), error.getMessage()))
			.toList());
		return ResponseEntity.badRequest()
			.body(problem);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ProblemDetail> malformedBody(HttpMessageNotReadableException exception,
		HttpServletRequest request) {
		return response(HttpStatus.BAD_REQUEST, "malformed_request", "The request body is not valid JSON.", request);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<ProblemDetail> invalidParameter(MethodArgumentTypeMismatchException exception,
		HttpServletRequest request) {
		return response(HttpStatus.BAD_REQUEST, "invalid_parameter",
			"The request parameter '" + exception.getName() + "' has an invalid value.", request);
	}

	@ExceptionHandler(EntityNotFoundException.class)
	ResponseEntity<ProblemDetail> notFound(EntityNotFoundException exception, HttpServletRequest request) {
		return response(HttpStatus.NOT_FOUND, "resource_not_found", exception.getMessage(), request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<ProblemDetail> conflict(DataIntegrityViolationException exception, HttpServletRequest request) {
		return response(HttpStatus.CONFLICT, "data_conflict",
			"The operation conflicts with existing data.", request);
	}

	@ExceptionHandler(AccessDeniedException.class)
	ResponseEntity<ProblemDetail> forbidden(AccessDeniedException exception, HttpServletRequest request) {
		return response(HttpStatus.FORBIDDEN, "access_denied", "Access is denied.", request);
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ProblemDetail> unexpected(Exception exception, HttpServletRequest request) {
		LOG.error("Unhandled API error", exception);
		return response(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error",
			"An unexpected internal error occurred.", request);
	}

	private ResponseEntity<ProblemDetail> response(HttpStatus status, String code, String detail,
		HttpServletRequest request) {
		return ResponseEntity.status(status)
			.body(problems.create(status, code, detail, request));
	}

	public record FieldViolation(String field, String message) {
	}
}
