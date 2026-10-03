package com.utp.clinitech.dto;

import java.time.OffsetDateTime; // Usado para recibir la fecha y hora de la cita con zona horaria.
import com.utp.clinitech.model.enums.PrioridadCita; // Usado para recibir el nivel de urgencia o prioridad asignada en el triaje.
import jakarta.validation.constraints.Future; // Usado para validar que la cita médica se reserve para una fecha futura.
import jakarta.validation.constraints.NotBlank; // Usado para validar que el motivo de consulta no esté vacío.
import jakarta.validation.constraints.NotNull; // Usado para validar que médico, fecha y prioridad sean campos obligatorios.
import jakarta.validation.constraints.Size; // Usado para limitar el tamaño del motivo de consulta a 255 caracteres.

// DTO inmutable que encapsula la información requerida para agendar una nueva cita médica.
public record CitaRequest(
    Long pacienteId, 
    @NotNull Long medicoId, 
    @NotNull @Future OffsetDateTime fechaHora,
    @NotNull PrioridadCita prioridad, 
    @NotBlank @Size(max = 255) String motivoConsulta) {
}
