package com.utp.clinitech.exception;

import org.springframework.http.HttpStatus; // Usado para asociar un código de estado HTTP específico al error producido.

// Excepción personalizada de negocio para reportar errores controlados en los servicios del backend.
public class ApiException extends RuntimeException {
  private final HttpStatus status; // Código HTTP representativo del error (400, 401, 403, 404, 409, etc.).

  // Constructor que inicializa el mensaje descriptivo y el estado HTTP de la excepción.
  public ApiException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }

  // Retorna el estado HTTP asociado a este error de negocio.
  public HttpStatus getStatus() {
    return status;
  }
}
