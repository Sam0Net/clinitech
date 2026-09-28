package com.utp.clinitech.dto;
import jakarta.validation.constraints.*;
public record ConsultaRequest(@NotNull Long citaId, @NotBlank @Size(max = 1000) String diagnostico,
  @NotBlank @Size(max = 1000) String tratamiento, @Size(max = 500) String observaciones) { }
