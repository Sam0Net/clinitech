package com.utp.clinitech.dto;

import java.time.OffsetDateTime;
import com.utp.clinitech.model.enums.EstadoCita;
import com.utp.clinitech.model.enums.PrioridadCita;

public record CitaResponse(Long id, Long pacienteId, String pacienteNombre, Long medicoId, String medicoNombre,
    OffsetDateTime fechaHora, EstadoCita estado, PrioridadCita prioridad, String motivoConsulta) {
}
