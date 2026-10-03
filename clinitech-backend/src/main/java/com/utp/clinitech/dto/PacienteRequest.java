package com.utp.clinitech.dto;

import java.time.LocalDate; // Usado para tipar la fecha de nacimiento del paciente.
import jakarta.validation.constraints.Email; // Usado para validar la estructura del correo electrónico.
import jakarta.validation.constraints.NotBlank; // Usado para validar que nombres, apellidos y DNI no estén en blanco.
import jakarta.validation.constraints.NotNull; // Usado para asegurar la obligatoriedad de la fecha de nacimiento.
import jakarta.validation.constraints.Past; // Usado para asegurar que la fecha de nacimiento sea histórica.
import jakarta.validation.constraints.Pattern; // Usado para validar el formato exacto de DNI (8 dígitos) y teléfono (9 dígitos).
import jakarta.validation.constraints.Size; // Usado para delimitar la cantidad máxima de caracteres permitidos.

// DTO para el registro administrativo y actualización de la ficha de datos de un paciente.
public record PacienteRequest(
    @NotBlank @Pattern(regexp = "\\d{8}") String dni, 
    @NotBlank @Size(max = 100) String nombres,
    @NotBlank @Size(max = 100) String apellidos, 
    @NotNull @Past LocalDate fechaNacimiento,
    @NotBlank @Pattern(regexp = "\\d{9}") String telefono, 
    @NotBlank @Email @Size(max = 100) String correo,
    @Size(max = 255) String direccion, 
    @Size(max = 500) String alergias, 
    @Size(max = 1000) String historialMedico) { 
}
