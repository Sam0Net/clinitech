package com.utp.clinitech.exception;

import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(ApiException.class)
  ResponseEntity<ApiError> api(ApiException ex, WebRequest request) {
    return response(ex.getStatus(), ex.getMessage(), Map.of(), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, WebRequest request) {
    Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
        .collect(java.util.stream.Collectors.toMap(FieldError::getField,
            e -> e.getDefaultMessage() == null ? "Valor inválido" : e.getDefaultMessage(), (a, b) -> a));
    return response(HttpStatus.BAD_REQUEST, "La solicitud contiene campos inválidos", fields, request);
  }

  @ExceptionHandler({ DataIntegrityViolationException.class, IllegalArgumentException.class })
  ResponseEntity<ApiError> conflict(Exception ex, WebRequest request) {
    return response(HttpStatus.CONFLICT, "La operación entra en conflicto con datos existentes", Map.of(), request);
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ApiError> denied(AccessDeniedException ex, WebRequest request) {
    return response(HttpStatus.FORBIDDEN, "No tienes permiso para esta operación", Map.of(), request);
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> unexpected(Exception ex, WebRequest request) {
    return response(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado", Map.of(), request);
  }

  private ResponseEntity<ApiError> response(HttpStatus status, String message, Map<String, String> fields,
      WebRequest request) {
    return ResponseEntity.status(status).body(new ApiError(OffsetDateTime.now(), status.value(), message,
        request.getDescription(false).replace("uri=", ""), fields));
  }

  public record ApiError(OffsetDateTime timestamp, int status, String message, String path,
      Map<String, String> fields) {
  }
}
