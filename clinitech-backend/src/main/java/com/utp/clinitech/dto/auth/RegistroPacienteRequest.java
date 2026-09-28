package com.utp.clinitech.dto.auth;

import java.time.LocalDate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroPacienteRequest(
    @NotBlank @Size(min = 4, max = 50) String username,
    @NotBlank @Size(min = 6, max = 128) String password,
    @NotBlank @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener 8 dígitos numéricos") String dni,
    @NotBlank @Size(max = 100) String nombres,
    @NotBlank @Size(max = 100) String apellidos,
    @NotNull @Past(message = "La fecha de nacimiento debe ser en el pasado") LocalDate fechaNacimiento,
    @NotBlank @Pattern(regexp = "^\\d{9}$", message = "El teléfono debe tener 9 dígitos") String telefono,
    @NotBlank @Email @Size(max = 100) String correo,
    @Size(max = 255) String direccion,
    @Size(max = 500) String alergias,
    @Size(max = 1000) String historialMedico
) { }
