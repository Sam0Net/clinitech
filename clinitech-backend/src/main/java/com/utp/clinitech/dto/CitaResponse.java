package com.utp.clinitech.dto;

import java.time.OffsetDateTime; // Usado para transferir la fecha y hora de la cita programada con zona horaria.
import com.utp.clinitech.model.enums.EstadoCita; // Usado para exponer el estado actual de la cita médica.
import com.utp.clinitech.model.enums.PrioridadCita; // Usado para exponer la urgencia o prioridad de triaje de la cita.

// DTO inmutable que retorna los datos completos y descriptivos de una cita médica al cliente.
public record CitaResponse(
    Long id, 
    Long pacienteId, 
    String pacienteNombre, 
    Long medicoId, 
    String medicoNombre,
    OffsetDateTime fechaHora, 
    EstadoCita estado, 
    PrioridadCita prioridad, 
    String motivoConsulta) {
}
