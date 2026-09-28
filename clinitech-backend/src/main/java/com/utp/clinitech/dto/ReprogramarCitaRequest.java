package com.utp.clinitech.dto;
import java.time.OffsetDateTime;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
public record ReprogramarCitaRequest(@NotNull @Future OffsetDateTime fechaHora) { }
