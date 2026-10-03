package com.utp.clinitech.dto.auth;

import java.time.LocalDate; // Usado para registrar y validar la fecha de nacimiento del paciente.
import jakarta.validation.constraints.Email; // Usado para validar el formato de correo electrónico.
import jakarta.validation.constraints.NotBlank; // Usado para requerir que los campos de texto no estén vacíos.
import jakarta.validation.constraints.NotNull; // Usado para asegurar la presencia de la fecha de nacimiento.
import jakarta.validation.constraints.Past; // Usado para asegurar que la fecha de nacimiento corresponda al pasado.
import jakarta.validation.constraints.Pattern; // Usado para validar el formato exacto de DNI (8 dígitos) y teléfono (9 dígitos).
import jakarta.validation.constraints.Size; // Usado para delimitar las longitudes de texto permitidas.

// DTO con validaciones estrictas para el registro público de nuevos pacientes y creación de su cuenta de usuario.
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
    @Size(max = 1000) String historialMedico) {
}
