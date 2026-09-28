package com.utp.clinitech.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.*;

public record PacienteRequest(@NotBlank @Pattern(regexp = "\\d{8}") String dni, @NotBlank @Size(max = 100) String nombres,
  @NotBlank @Size(max = 100) String apellidos, @NotNull @Past LocalDate fechaNacimiento,
  @NotBlank @Pattern(regexp = "\\d{9}") String telefono, @NotBlank @Email @Size(max = 100) String correo,
  @Size(max = 255) String direccion, @Size(max = 500) String alergias, @Size(max = 1000) String historialMedico) { }
