package com.utp.clinitech.dto;

import java.time.LocalDate; // Usado para informar las fechas de inicio y fin del intervalo analizado.
import java.util.List; // Usado para contener el ranking de los médicos más demandados.

// DTO inmutable que consolida las métricas e indicadores de gestión administrativa del hospital.
public record ReporteAdministrativoResponse(
    LocalDate desde, 
    LocalDate hasta, 
    long totalCitas, 
    long citasAtendidas,
    long citasCanceladas, 
    long medicosActivos, 
    long pacientesActivos, 
    List<MedicoSolicitado> medicosMasSolicitados) {

  // Record anidado que representa a un médico en el ranking con su número de citas asociadas.
  public record MedicoSolicitado(Long medicoId, String nombre, long citas) { 
  }
}
