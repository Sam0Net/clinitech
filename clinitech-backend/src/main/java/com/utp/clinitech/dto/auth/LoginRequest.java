package com.utp.clinitech.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.utp.clinitech.model.enums.RolUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank @Size(max = 50) String username,
    @NotBlank @Size(max = 128) String password,
    @NotNull RolUsuario role
) {
  @JsonCreator
  public static LoginRequest of(
      @JsonProperty("username") String username,
      @JsonProperty("password") String password,
      @JsonProperty("role") @JsonAlias("rol") String roleStr
  ) {
    RolUsuario parsedRol = parseRol(roleStr);
    return new LoginRequest(username, password, parsedRol);
  }

  private static RolUsuario parseRol(String roleStr) {
    if (roleStr == null || roleStr.isBlank()) {
      return null;
    }
    String clean = roleStr.trim().toUpperCase();
    return switch (clean) {
      case "PATIENT", "PACIENTE" -> RolUsuario.PACIENTE;
      case "DOCTOR", "MEDICO", "MÉDICO" -> RolUsuario.MEDICO;
      case "ADMIN", "ADMINISTRADOR" -> RolUsuario.ADMIN;
      default -> throw new IllegalArgumentException("Rol no válido: " + roleStr);
    };
  }
}
