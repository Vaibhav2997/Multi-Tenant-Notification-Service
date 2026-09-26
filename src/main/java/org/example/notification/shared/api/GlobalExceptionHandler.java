package org.example.notification.shared.api;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail notFound(NotFoundException exception) {
    return problem(HttpStatus.NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler(ConflictException.class)
  public ProblemDetail conflict(ConflictException exception) {
    return problem(HttpStatus.CONFLICT, exception.getCode(), exception.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail badRequest(IllegalArgumentException exception) {
    return problem(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_FAILED, exception.getMessage());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail malformedRequest(HttpMessageNotReadableException exception) {
    return problem(
        HttpStatus.BAD_REQUEST,
        ApiErrorCode.MALFORMED_REQUEST,
        "Request body contains malformed JSON");
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail forbidden(AccessDeniedException exception) {
    return problem(HttpStatus.FORBIDDEN, ApiErrorCode.ACCESS_DENIED, "Access is denied");
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ProblemDetail unauthorized(BadCredentialsException exception) {
    return problem(
        HttpStatus.UNAUTHORIZED, ApiErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail validation(MethodArgumentNotValidException exception) {
    ProblemDetail detail =
        problem(
            HttpStatus.BAD_REQUEST,
            ApiErrorCode.VALIDATION_FAILED,
            "One or more request fields are invalid");
    Map<String, String> errors = new LinkedHashMap<>();
    exception
        .getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
    detail.setProperty("fieldErrors", errors);
    return detail;
  }

  private ProblemDetail problem(HttpStatus status, ApiErrorCode code, String message) {
    ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
    detail.setTitle(code.name());
    detail.setType(
        URI.create("https://notification-service.local/problems/" + code.name().toLowerCase()));
    detail.setProperty("code", code.name());
    return detail;
  }
}
