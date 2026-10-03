package com.utp.clinitech.exception;

import java.time.OffsetDateTime; // Usado para estampar la fecha y hora en que ocurrió el error con zona horaria.
import java.util.Map; // Usado para mapear los campos erróneos y sus respectivos mensajes de validación.
import org.springframework.dao.DataIntegrityViolationException; // Usado para interceptar fallos de integridad referencial o unicidad en la base de datos.
import org.springframework.http.HttpStatus; // Usado para definir los códigos numéricos de estado de respuesta HTTP.
import org.springframework.http.ResponseEntity; // Usado para estructurar la respuesta HTTP unificada de error.
import org.springframework.security.access.AccessDeniedException; // Usado para interceptar accesos no autorizados por falta de permisos.
import org.springframework.validation.FieldError; // Usado para extraer el nombre del campo que falló la validación.
import org.springframework.web.bind.MethodArgumentNotValidException; // Usado para capturar fallos de validación en DTOs anotados con @Valid.
import org.springframework.web.bind.annotation.ExceptionHandler; // Usado para declarar los métodos manejadores de cada tipo de excepción.
import org.springframework.web.bind.annotation.RestControllerAdvice; // Usado para interceptar excepciones globalmente en todos los controladores REST.
import org.springframework.web.context.request.WebRequest; // Usado para obtener la ruta o URI de la petición que generó el fallo.

// Manejador centralizado de excepciones para uniformizar las respuestas de error JSON de la API.
@RestControllerAdvice
public class ApiExceptionHandler {

  // Captura excepciones personalizadas de negocio ApiException.
  @ExceptionHandler(ApiException.class)
  ResponseEntity<ApiError> api(ApiException ex, WebRequest request) {
    return response(ex.getStatus(), ex.getMessage(), Map.of(), request);
  }

  // Captura errores cuando fallan las validaciones de los DTOs (@Valid).
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, WebRequest request) {
    Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
        .collect(java.util.stream.Collectors.toMap(FieldError::getField,
            e -> e.getDefaultMessage() == null ? "Valor inválido" : e.getDefaultMessage(), (a, b) -> a));
    return response(HttpStatus.BAD_REQUEST, "La solicitud contiene campos inválidos", fields, request);
  }

  // Captura conflictos de unicidad en base de datos o argumentos ilegales.
  @ExceptionHandler({ DataIntegrityViolationException.class, IllegalArgumentException.class })
  ResponseEntity<ApiError> conflict(Exception ex, WebRequest request) {
    return response(HttpStatus.CONFLICT, "La operación entra en conflicto con datos existentes", Map.of(), request);
  }

  // Captura denegaciones de acceso por insuficiencia de permisos de rol.
  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ApiError> denied(AccessDeniedException ex, WebRequest request) {
    return response(HttpStatus.FORBIDDEN, "No tienes permiso para esta operación", Map.of(), request);
  }

  // Captura cualquier otro error no previsto para evitar fugas de traza internas.
  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> unexpected(Exception ex, WebRequest request) {
    return response(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado", Map.of(), request);
  }

  // Construye el objeto de error normalizado ApiError con metadatos.
  private ResponseEntity<ApiError> response(HttpStatus status, String message, Map<String, String> fields,
      WebRequest request) {
    return ResponseEntity.status(status).body(new ApiError(OffsetDateTime.now(), status.value(), message,
        request.getDescription(false).replace("uri=", ""), fields));
  }

  // Estructura de datos inmutable para la respuesta estándar de error HTTP.
  public record ApiError(OffsetDateTime timestamp, int status, String message, String path,
      Map<String, String> fields) {
  }
}
