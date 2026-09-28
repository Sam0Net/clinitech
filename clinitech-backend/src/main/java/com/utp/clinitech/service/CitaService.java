package com.utp.clinitech.service;

import java.time.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.utp.clinitech.dao.CitaDAO;
import com.utp.clinitech.dao.PacienteDAO;
import com.utp.clinitech.dto.*;
import com.utp.clinitech.exception.ApiException;
import com.utp.clinitech.model.*;
import com.utp.clinitech.model.enums.*;

@Service
@Transactional(readOnly = true)
public class CitaService {
  private static final ZoneId CLINIC_ZONE = ZoneId.of("America/Lima");
  private final CitaDAO citas;
  private final PacienteDAO pacientes;
  private final MedicoService medicos;
  private final CurrentUserService currentUser;

  public CitaService(CitaDAO citas, PacienteDAO pacientes, MedicoService medicos, CurrentUserService currentUser) {
    this.citas = citas;
    this.pacientes = pacientes;
    this.medicos = medicos;
    this.currentUser = currentUser;
  }

  @Transactional
  public CitaResponse crear(CitaRequest request) {
    Usuario actor = currentUser.required();
    Long pacienteId = request.pacienteId();
    if (actor.getRol() == RolUsuario.PACIENTE) {
      if (actor.getPaciente() == null)
        throw new ApiException(HttpStatus.FORBIDDEN, "La cuenta no está vinculada a un paciente");
      if (pacienteId != null && !pacienteId.equals(actor.getPaciente().getId()))
        throw new ApiException(HttpStatus.FORBIDDEN, "No puedes agendar para otro paciente");
      pacienteId = actor.getPaciente().getId();
    } else
      currentUser.requireRole(actor, RolUsuario.ADMIN);
    if (pacienteId == null)
      throw new ApiException(HttpStatus.BAD_REQUEST, "Debe indicar un paciente");
    Medico medico = medicos.entidad(request.medicoId());
    if (citas.existsByMedicoIdAndFechaHora(medico.getId(), request.fechaHora()))
      throw new ApiException(HttpStatus.CONFLICT, "El horario seleccionado ya no está disponible");
    Cita cita = new Cita(pacientesEntidad(pacienteId), medico, request.fechaHora(), request.prioridad(),
        request.motivoConsulta().trim());
    return ApiMapper.cita(citas.save(cita));
  }

  public List<CitaResponse> listar(Long pacienteId, Long medicoId) {
    Usuario actor = currentUser.required();
    List<Cita> result;
    if (actor.getRol() == RolUsuario.PACIENTE) {
      if (actor.getPaciente() == null)
        throw new ApiException(HttpStatus.FORBIDDEN, "La cuenta no está vinculada a un paciente");
      result = citas.findByPacienteIdOrderByFechaHoraDesc(actor.getPaciente().getId());
    } else if (actor.getRol() == RolUsuario.MEDICO) {
      if (actor.getMedico() == null)
        throw new ApiException(HttpStatus.FORBIDDEN, "La cuenta no está vinculada a un médico");
      result = citas.findByMedicoIdOrderByFechaHoraDesc(actor.getMedico().getId());
    } else if (pacienteId != null)
      result = citas.findByPacienteIdOrderByFechaHoraDesc(pacienteId);
    else if (medicoId != null)
      result = citas.findByMedicoIdOrderByFechaHoraDesc(medicoId);
    else
      result = citas.findAll();
    return result.stream().map(ApiMapper::cita).toList();
  }

  public List<OffsetDateTime> disponibilidad(Long medicoId, LocalDate fecha) {
    currentUser.required();
    Medico medico = medicos.entidad(medicoId);
    OffsetDateTime inicio = fecha.atStartOfDay(CLINIC_ZONE).toOffsetDateTime();
    OffsetDateTime fin = fecha.plusDays(1).atStartOfDay(CLINIC_ZONE).toOffsetDateTime();
    List<OffsetDateTime> ocupados = citas.findByMedicoIdAndFechaHoraBetweenOrderByFechaHoraAsc(medicoId, inicio, fin)
        .stream()
        .filter(c -> c.getEstado() != EstadoCita.CANCELADA).map(Cita::getFechaHora).toList();

    int[] rango = obtenerRangoSlots(medico.getHorarioAtencion());
    return java.util.stream.IntStream.range(rango[0], rango[1])
        .mapToObj(i -> fecha.atTime(i / 2, i % 2 * 30).atZone(CLINIC_ZONE).toOffsetDateTime())
        .filter(slot -> slot.isAfter(OffsetDateTime.now()) && !ocupados.contains(slot)).toList();
  }

  public List<CitaResponse> colaAtencion(Long medicoId, LocalDate fecha) {
    validarAccesoMedicoOAdmin(medicoId);
    com.utp.clinitech.util.ColaPrioridad cola = construirColaDelDia(medicoId, fecha);
    return cola.ordenadas().stream().map(ApiMapper::cita).toList();
  }

  public CitaResponse siguienteEnCola(Long medicoId, LocalDate fecha) {
    validarAccesoMedicoOAdmin(medicoId);
    com.utp.clinitech.util.ColaPrioridad cola = construirColaDelDia(medicoId, fecha);
    Cita siguiente = cola.siguiente()
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No hay citas pendientes en la cola de atención"));
    return ApiMapper.cita(siguiente);
  }

  private com.utp.clinitech.util.ColaPrioridad construirColaDelDia(Long medicoId, LocalDate fecha) {
    LocalDate target = fecha != null ? fecha : LocalDate.now(CLINIC_ZONE);
    OffsetDateTime inicio = target.atStartOfDay(CLINIC_ZONE).toOffsetDateTime();
    OffsetDateTime fin = target.plusDays(1).atStartOfDay(CLINIC_ZONE).toOffsetDateTime();

    List<Cita> delDia = citas.findByMedicoIdAndFechaHoraBetweenOrderByFechaHoraAsc(medicoId, inicio, fin).stream()
        .filter(c -> c.getEstado() == EstadoCita.PENDIENTE || c.getEstado() == EstadoCita.CONFIRMADA
            || c.getEstado() == EstadoCita.REPROGRAMADA)
        .toList();

    com.utp.clinitech.util.ColaPrioridad cola = new com.utp.clinitech.util.ColaPrioridad();
    for (Cita c : delDia) {
      cola.encolar(c);
    }
    return cola;
  }

  private void validarAccesoMedicoOAdmin(Long medicoId) {
    Usuario actor = currentUser.required();
    if (actor.getRol() == RolUsuario.ADMIN)
      return;
    if (actor.getRol() == RolUsuario.MEDICO && actor.getMedico() != null && actor.getMedico().getId().equals(medicoId))
      return;
    throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para acceder a la cola de atención de este médico");
  }

  private int[] obtenerRangoSlots(String horarioAtencion) {
    if (horarioAtencion != null) {
      try {
        String[] partes = horarioAtencion.split("[-aA]");
        if (partes.length >= 2) {
          int hInicio = Integer.parseInt(partes[0].trim().substring(0, 2));
          int hFin = Integer.parseInt(partes[1].trim().substring(0, 2));
          if (hInicio >= 6 && hFin <= 22 && hInicio < hFin) {
            return new int[] { hInicio * 2, hFin * 2 };
          }
        }
      } catch (Exception ignored) {
      }
    }
    return new int[] { 16, 36 }; // Default 08:00 a 18:00
  }

  @Transactional
  public CitaResponse reprogramar(Long id, ReprogramarCitaRequest request) {
    Cita cita = obtenerEntidad(id);
    Usuario actor = currentUser.required();
    validarPacienteOAdmin(actor, cita);
    if (cita.getEstado() == EstadoCita.ATENDIDA || cita.getEstado() == EstadoCita.CANCELADA)
      throw new ApiException(HttpStatus.CONFLICT, "La cita no puede reprogramarse");
    if (citas.existsByMedicoIdAndFechaHora(cita.getMedico().getId(), request.fechaHora()))
      throw new ApiException(HttpStatus.CONFLICT, "El horario seleccionado ya no está disponible");
    cita.reprogramar(request.fechaHora());
    return ApiMapper.cita(cita);
  }

  @Transactional
  public CitaResponse cambiarEstado(Long id, CambiarEstadoCitaRequest request) {
    Cita cita = obtenerEntidad(id);
    Usuario actor = currentUser.required();
    if (actor.getRol() == RolUsuario.PACIENTE) {
      validarPacienteOAdmin(actor, cita);
      if (request.estado() != EstadoCita.CANCELADA)
        throw new ApiException(HttpStatus.FORBIDDEN, "Un paciente sólo puede cancelar su cita");
    } else if (actor.getRol() == RolUsuario.MEDICO) {
      if (actor.getMedico() == null || !actor.getMedico().getId().equals(cita.getMedico().getId()))
        throw new ApiException(HttpStatus.FORBIDDEN, "No puedes actualizar esta cita");
    } else
      currentUser.requireRole(actor, RolUsuario.ADMIN);
    if (cita.getEstado() == EstadoCita.ATENDIDA)
      throw new ApiException(HttpStatus.CONFLICT, "Una cita atendida no puede cambiar de estado");
    cita.cambiarEstado(request.estado());
    return ApiMapper.cita(cita);
  }

  public Cita obtenerEntidad(Long id) {
    return citas.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
  }

  private Paciente pacientesEntidad(Long id) {
    return pacientes.findById(id).filter(Paciente::isActivo)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
  }

  private void validarPacienteOAdmin(Usuario actor, Cita cita) {
    if (actor.getRol() == RolUsuario.ADMIN)
      return;
    if (actor.getRol() == RolUsuario.PACIENTE && actor.getPaciente() != null
        && actor.getPaciente().getId().equals(cita.getPaciente().getId()))
      return;
    throw new ApiException(HttpStatus.FORBIDDEN, "No puedes modificar esta cita");
  }
}
