package com.utp.clinitech.dto;
import java.time.LocalDate;
import java.util.List;
public record ReporteAdministrativoResponse(LocalDate desde, LocalDate hasta, long totalCitas, long citasAtendidas,
  long citasCanceladas, long medicosActivos, long pacientesActivos, List<MedicoSolicitado> medicosMasSolicitados) {
  public record MedicoSolicitado(Long medicoId, String nombre, long citas) { }
}
