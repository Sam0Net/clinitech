package com.utp.clinitech.dto;

import com.utp.clinitech.model.enums.PrioridadCita;
import jakarta.validation.constraints.NotNull;

// DTO para solicitar el cambio de nivel de prioridad (triaje) de una cita médica.
public record CambiarPrioridadCitaRequest(
    @NotNull PrioridadCita prioridad) {
}
