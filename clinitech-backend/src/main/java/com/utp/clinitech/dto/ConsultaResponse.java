package com.utp.clinitech.dto;

import java.time.OffsetDateTime; // Usado para retornar la fecha y hora exacta de la atención clínica con zona horaria.

// DTO inmutable para exponer los resultados, diagnóstico y receta de una consulta médica realizada.
public record ConsultaResponse(
    Long id, 
    Long citaId, 
    Long pacienteId, 
    String medicoNombre, 
    String diagnostico,
    String tratamiento, 
    String observaciones, 
    OffsetDateTime fechaAtencion) {
}
