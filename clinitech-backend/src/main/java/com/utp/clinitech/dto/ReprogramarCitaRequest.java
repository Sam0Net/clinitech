package com.utp.clinitech.dto;

import java.time.OffsetDateTime; // Usado para recibir la nueva fecha y hora solicitada con zona horaria.
import jakarta.validation.constraints.Future; // Usado para asegurar que la nueva fecha solicitada sea posterior al momento actual.
import jakarta.validation.constraints.NotNull; // Usado para garantizar la obligatoriedad de la nueva fecha y hora.

// DTO para solicitar la reprogramación de una cita médica existente a un nuevo horario.
public record ReprogramarCitaRequest(
    @NotNull @Future OffsetDateTime fechaHora) { 
}
