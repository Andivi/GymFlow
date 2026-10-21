package com.gymflow.common.error;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Renders every API error as an RFC 9457 problem document with a stable
 * {@code code} property that clients can switch on.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "One or more fields are invalid.");
        problem.setTitle("Validation failed");
        problem.setProperty("code", ErrorCode.VALIDATION_FAILED);
        problem.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
            .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
            .toList());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Not found");
        problem.setProperty("code", ErrorCode.NOT_FOUND);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        // Never echo the exception message: it may contain SQL, hostnames or tenant routing details.
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        problem.setTitle("Internal server error");
        problem.setProperty("code", ErrorCode.INTERNAL_ERROR);
        return problem;
    }

    @Override
    protected ResponseEntity<Object> createResponseEntity(
            @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

        // Framework-raised errors (404 route, 405, malformed JSON) get a code too.
        if (body instanceof ProblemDetail problem
                && (problem.getProperties() == null || !problem.getProperties().containsKey("code"))) {
            problem.setProperty("code", ErrorCode.forStatus(statusCode));
        }
        return super.createResponseEntity(body, headers, statusCode, request);
    }

    record FieldViolation(String field, String message) {}

    static final class ErrorCode {
        static final String VALIDATION_FAILED = "VALIDATION_FAILED";
        static final String NOT_FOUND = "NOT_FOUND";
        static final String BAD_REQUEST = "BAD_REQUEST";
        static final String INTERNAL_ERROR = "INTERNAL_ERROR";

        static String forStatus(HttpStatusCode status) {
            if (status.value() == HttpStatus.NOT_FOUND.value()) {
                return NOT_FOUND;
            }
            return status.is4xxClientError() ? BAD_REQUEST : INTERNAL_ERROR;
        }

        private ErrorCode() {}
    }
}
