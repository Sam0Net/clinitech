package com.utp.clinitech.dto;
import jakarta.validation.constraints.*;
public record MedicoRequest(@NotBlank @Size(max = 100) String nombres, @NotBlank @Size(max = 100) String apellidos,
  @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{5,10}") String cmp, @NotNull Long especialidadId,
  @NotBlank @Pattern(regexp = "\\d{9}") String telefono, @NotBlank @Email @Size(max = 100) String correo,
  @NotBlank @Size(max = 100) String horarioAtencion) { }
