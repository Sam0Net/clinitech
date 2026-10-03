package com.utp.clinitech.dto;

import jakarta.validation.constraints.NotBlank; // Usado para asegurar que el nombre de la especialidad no sea vacío.
import jakarta.validation.constraints.Size; // Usado para delimitar la longitud máxima del nombre y descripción.

// DTO para la creación y edición de especialidades médicas en el catálogo de la clínica.
public record EspecialidadRequest(
    @NotBlank @Size(max = 100) String nombre, 
    @Size(max = 255) String descripcion) {
}
