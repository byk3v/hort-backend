package com.kubuci.hort.shared.error;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ApiProblemFactory {

	public ProblemDetail create(HttpStatus status, String code, String detail, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(status.getReasonPhrase());
		problem.setType(URI.create("urn:hort:error:" + code));
		problem.setInstance(URI.create(request.getRequestURI()));
		problem.setProperty("code", code);
		problem.setProperty("traceId", traceId(request));
		return problem;
	}

	private String traceId(HttpServletRequest request) {
		String requestId = request.getHeader("X-Request-ID");
		return requestId == null || requestId.isBlank()
			? UUID.randomUUID()
				.toString()
			: requestId;
	}
}
