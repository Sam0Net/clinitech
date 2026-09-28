package com.utp.clinitech.dto;
import com.utp.clinitech.model.enums.EstadoCita;
import jakarta.validation.constraints.NotNull;
public record CambiarEstadoCitaRequest(@NotNull EstadoCita estado) { }
