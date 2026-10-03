package com.utp.clinitech.dto;

import jakarta.validation.constraints.NotBlank; // Usado para validar que el diagnóstico y tratamiento contengan texto explicativo.
import jakarta.validation.constraints.NotNull; // Usado para validar la obligatoriedad del identificador de la cita asociada.
import jakarta.validation.constraints.Size; // Usado para delimitar la cantidad máxima de caracteres de los textos clínicos.

// DTO para recibir el registro de la atención médica, diagnóstico y tratamiento prescrito.
public record ConsultaRequest(
    @NotNull Long citaId, 
    @NotBlank @Size(max = 1000) String diagnostico,
    @NotBlank @Size(max = 1000) String tratamiento, 
    @Size(max = 500) String observaciones) {
}
