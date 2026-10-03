package com.utp.clinitech.dto;

import com.utp.clinitech.model.enums.EstadoCita; // Usado para tipar el nuevo estado de la cita (ej. ATENDIDA o CANCELADA).
import jakarta.validation.constraints.NotNull; // Usado para asegurar que el nuevo estado sea obligatorio y no nulo.

// DTO para solicitar el cambio de estado en el ciclo de vida de una cita médica.
public record CambiarEstadoCitaRequest(
    @NotNull EstadoCita estado) {
}
