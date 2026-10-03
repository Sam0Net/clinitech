package com.utp.clinitech.service;

import java.time.LocalDate; // Usado para representar los límites de fecha del reporte solicitado.
import java.time.OffsetDateTime; // Usado para calcular marcas de tiempo con inicio y fin de día con zona horaria.
import java.time.ZoneId; // Usado para ajustar las fechas a la zona horaria de Lima (America/Lima).
import java.time.temporal.ChronoUnit; // Usado para calcular la diferencia en días y validar el rango máximo permitido.
import java.util.Comparator; // Usado para ordenar el ranking de médicos según cantidad de citas atendidas.
import java.util.HashMap; // Usado para agrupar las citas por identificador de médico.
import java.util.List; // Usado para manipular colecciones de citas y resultados del top de médicos.
import java.util.Map; // Usado para estructurar el mapa de agregación de métricas.
import org.springframework.http.HttpStatus; // Usado para devolver 400 BAD_REQUEST si las fechas son inválidas.
import org.springframework.stereotype.Service; // Usado para registrar la clase como componente de servicio en Spring.
import org.springframework.transaction.annotation.Transactional; // Usado para ejecutar las lecturas en modo de solo lectura.
import com.utp.clinitech.dao.CitaDAO; // Usado para consultar las citas comprendidas en el rango de fechas.
import com.utp.clinitech.dao.MedicoDAO; // Usado para obtener el total de médicos activos del hospital.
import com.utp.clinitech.dao.PacienteDAO; // Usado para obtener el total de pacientes activos registrados.
import com.utp.clinitech.dto.ReporteAdministrativoResponse; // Usado para estructurar el reporte de salida para el Administrador.
import com.utp.clinitech.exception.ApiException; // Usado para lanzar errores de validación de fechas del reporte.
import com.utp.clinitech.model.Cita; // Usado para procesar cada cita y sus estados.
import com.utp.clinitech.model.enums.EstadoCita; // Usado para filtrar citas según condición ATENDIDA o CANCELADA.
import com.utp.clinitech.model.enums.RolUsuario; // Usado para asegurar que solo el ADMIN pueda consultar reportes.

// Servicio responsable de compilar estadísticas operativas y ranking de atención hospitalaria.
@Service
@Transactional(readOnly = true)
public class ReporteService {
  private final CitaDAO citas; 
  private final MedicoDAO medicos; 
  private final PacienteDAO pacientes; 
  private final CurrentUserService currentUser;

  public ReporteService(CitaDAO citas, MedicoDAO medicos, PacienteDAO pacientes, CurrentUserService currentUser) { 
    this.citas = citas; 
    this.medicos = medicos; 
    this.pacientes = pacientes; 
    this.currentUser = currentUser; 
  }

  // Genera el consolidado de métricas de citas, pacientes y top 5 de médicos con más demanda en un período.
  public ReporteAdministrativoResponse resumen(LocalDate desde, LocalDate hasta) {
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN);
    if (hasta.isBefore(desde) || ChronoUnit.DAYS.between(desde, hasta) > 366) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "El rango de fechas es inválido o excede un año");
    }
    OffsetDateTime inicio = desde.atStartOfDay(ZoneId.of("America/Lima")).toOffsetDateTime();
    OffsetDateTime fin = hasta.plusDays(1).atStartOfDay(ZoneId.of("America/Lima")).toOffsetDateTime();
    List<Cita> periodo = citas.findByFechaHoraBetween(inicio, fin);
    Map<Long, ReporteAdministrativoResponse.MedicoSolicitado> grouped = new HashMap<>();
    for (Cita cita : periodo) {
      grouped.merge(cita.getMedico().getId(), 
          new ReporteAdministrativoResponse.MedicoSolicitado(cita.getMedico().getId(), cita.getMedico().nombreCompleto(), 1), 
          (a, b) -> new ReporteAdministrativoResponse.MedicoSolicitado(a.medicoId(), a.nombre(), a.citas() + 1));
    }
    List<ReporteAdministrativoResponse.MedicoSolicitado> top = grouped.values().stream()
        .sorted(Comparator.comparingLong(ReporteAdministrativoResponse.MedicoSolicitado::citas).reversed())
        .limit(5).toList(); // Extrae los 5 médicos más solicitados.

    return new ReporteAdministrativoResponse(
        desde, 
        hasta, 
        periodo.size(), 
        periodo.stream().filter(c -> c.getEstado() == EstadoCita.ATENDIDA).count(), 
        periodo.stream().filter(c -> c.getEstado() == EstadoCita.CANCELADA).count(), 
        medicos.countByActivoTrue(), 
        pacientes.countByActivoTrue(), 
        top
    );
  }
}
