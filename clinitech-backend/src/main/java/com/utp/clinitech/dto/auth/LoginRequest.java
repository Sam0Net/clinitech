package com.utp.clinitech.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias; // Usado para permitir nombres alternativos en la deserialización de campos JSON (ej. "rol").
import com.fasterxml.jackson.annotation.JsonCreator; // Usado para indicar el método fábrica personalizado usado por Jackson para instanciar el record.
import com.fasterxml.jackson.annotation.JsonProperty; // Usado para mapear explícitamente nombres de propiedades JSON con los parámetros del constructor.
import com.utp.clinitech.model.enums.RolUsuario; // Usado para tipar el rol con el que el usuario intenta iniciar sesión.
import jakarta.validation.constraints.NotBlank; // Usado para validar que las credenciales no estén vacías ni contengan solo espacios en blanco.
import jakarta.validation.constraints.NotNull; // Usado para validar la obligatoriedad del rol solicitado.
import jakarta.validation.constraints.Size; // Usado para delimitar la longitud máxima permitida para usuario y contraseña.

// DTO que transporta las credenciales y el rol para la autenticación de usuarios.
public record LoginRequest(
    @NotBlank @Size(max = 50) String username,
    @NotBlank @Size(max = 128) String password,
    @NotNull RolUsuario role) {

  // Fábrica JSON tolerante que acepta nombres de roles tanto en inglés como en español.
  @JsonCreator
  public static LoginRequest of(
      @JsonProperty("username") String username,
      @JsonProperty("password") String password,
      @JsonProperty("role") @JsonAlias("rol") String roleStr) {
    RolUsuario parsedRol = parseRol(roleStr); // Normaliza y traduce el rol recibido.
    return new LoginRequest(username, password, parsedRol);
  }

  // Convierte cadenas flexibles de roles a las constantes tipadas de la enumeración RolUsuario.
  private static RolUsuario parseRol(String roleStr) {
    if (roleStr == null || roleStr.isBlank()) {
      return null;
    }
    String clean = roleStr.trim().toUpperCase();
    return switch (clean) {
      case "PATIENT", "PACIENTE" -> RolUsuario.PACIENTE;
      case "DOCTOR", "MEDICO", "MÉDICO" -> RolUsuario.MEDICO;
      case "ADMIN", "ADMINISTRADOR" -> RolUsuario.ADMIN;
      case "RECEPCIONISTA", "RECEPCION", "RECEPTIONIST" -> RolUsuario.RECEPCIONISTA;
      default -> throw new IllegalArgumentException("Rol no válido: " + roleStr);
    };
  }
}
