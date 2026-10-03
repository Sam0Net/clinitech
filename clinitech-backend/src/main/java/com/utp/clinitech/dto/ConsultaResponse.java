package com.utp.clinitech.dto;

import java.time.OffsetDateTime;

public record ConsultaResponse(Long id, Long citaId, Long pacienteId, String medicoNombre, String diagnostico,
    String tratamiento, String observaciones, OffsetDateTime fechaAtencion) {
}
