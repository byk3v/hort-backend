package com.kubuci.hort.shared.error;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityProblemHandlers {

	private final ApiProblemFactory problems;
	private final ObjectMapper objectMapper;

	public SecurityProblemHandlers(ApiProblemFactory problems, ObjectMapper objectMapper) {
		this.problems = problems;
		this.objectMapper = objectMapper;
	}

	public void authenticationRequired(HttpServletRequest request, HttpServletResponse response, Exception exception)
		throws IOException {
		write(response, problems.create(HttpStatus.UNAUTHORIZED, "authentication_required",
			"A valid bearer token is required.", request));
	}

	public void accessDenied(HttpServletRequest request, HttpServletResponse response, Exception exception)
		throws IOException {
		write(response, problems.create(HttpStatus.FORBIDDEN, "access_denied", "Access is denied.", request));
	}

	private void write(HttpServletResponse response, Object problem) throws IOException {
		response.setStatus(problem instanceof org.springframework.http.ProblemDetail detail
			? detail.getStatus()
			: HttpStatus.INTERNAL_SERVER_ERROR.value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), problem);
	}
}
