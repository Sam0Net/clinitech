package com.utp.clinitech.service;

import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.utp.clinitech.dao.*;
import com.utp.clinitech.dto.ReporteAdministrativoResponse;
import com.utp.clinitech.exception.ApiException;
import com.utp.clinitech.model.Cita;
import com.utp.clinitech.model.enums.*;

@Service
@Transactional(readOnly = true)
public class ReporteService {
  private final CitaDAO citas; private final MedicoDAO medicos; private final PacienteDAO pacientes; private final CurrentUserService currentUser;
  public ReporteService(CitaDAO citas, MedicoDAO medicos, PacienteDAO pacientes, CurrentUserService currentUser) { this.citas = citas; this.medicos = medicos; this.pacientes = pacientes; this.currentUser = currentUser; }
  public ReporteAdministrativoResponse resumen(LocalDate desde, LocalDate hasta) {
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN);
    if (hasta.isBefore(desde) || java.time.temporal.ChronoUnit.DAYS.between(desde, hasta) > 366) throw new ApiException(HttpStatus.BAD_REQUEST, "El rango de fechas es inválido o excede un año");
    OffsetDateTime inicio = desde.atStartOfDay(ZoneId.of("America/Lima")).toOffsetDateTime();
    OffsetDateTime fin = hasta.plusDays(1).atStartOfDay(ZoneId.of("America/Lima")).toOffsetDateTime();
    List<Cita> periodo = citas.findByFechaHoraBetween(inicio, fin);
    Map<Long, ReporteAdministrativoResponse.MedicoSolicitado> grouped = new HashMap<>();
    for (Cita cita : periodo) grouped.merge(cita.getMedico().getId(), new ReporteAdministrativoResponse.MedicoSolicitado(cita.getMedico().getId(), cita.getMedico().nombreCompleto(), 1), (a, b) -> new ReporteAdministrativoResponse.MedicoSolicitado(a.medicoId(), a.nombre(), a.citas() + 1));
    List<ReporteAdministrativoResponse.MedicoSolicitado> top = grouped.values().stream().sorted(Comparator.comparingLong(ReporteAdministrativoResponse.MedicoSolicitado::citas).reversed()).limit(5).toList();
    return new ReporteAdministrativoResponse(desde, hasta, periodo.size(), periodo.stream().filter(c -> c.getEstado() == EstadoCita.ATENDIDA).count(), periodo.stream().filter(c -> c.getEstado() == EstadoCita.CANCELADA).count(), medicos.countByActivoTrue(), pacientes.countByActivoTrue(), top);
  }
}
