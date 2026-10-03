package com.utp.clinitech.dto;

import java.time.OffsetDateTime;
import com.utp.clinitech.model.enums.PrioridadCita;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CitaRequest(Long pacienteId, @NotNull Long medicoId, @NotNull @Future OffsetDateTime fechaHora,
    @NotNull PrioridadCita prioridad, @NotBlank @Size(max = 255) String motivoConsulta) {
}
