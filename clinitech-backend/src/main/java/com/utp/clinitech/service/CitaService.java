package com.utp.clinitech.service;

import java.time.LocalDate; // Fecha local sin zona horaria
import java.time.OffsetDateTime; // Fecha y hora con zona horaria
import java.time.ZoneId; // Zona horaria específica
import java.util.List; // Listas de objetos
import java.util.stream.IntStream; // Stream de enteros para operaciones funcionales
import org.springframework.http.HttpStatus; // Códigos de estado HTTP para respuestas
import org.springframework.stereotype.Service; // Marca la clase como un servicio de Spring
import org.springframework.transaction.annotation.Transactional; // Manejo de transacciones en métodos de servicio
import com.utp.clinitech.dao.CitaDAO; // Acceso a datos para la entidad Cita
import com.utp.clinitech.dao.PacienteDAO; // Acceso a datos para la entidad Paciente
import com.utp.clinitech.dto.CambiarEstadoCitaRequest; // DTO para cambiar el estado de una cita
import com.utp.clinitech.dto.CitaRequest; // DTO para crear una nueva cita
import com.utp.clinitech.dto.CitaResponse; // DTO para la respuesta de una cita
import com.utp.clinitech.dto.ReprogramarCitaRequest; // DTO para reprogramar una cita
import com.utp.clinitech.exception.ApiException; // Excepción personalizada para errores de API
import com.utp.clinitech.model.Cita; // Modelo de la entidad Cita
import com.utp.clinitech.model.Medico; // Modelo de la entidad Medico
import com.utp.clinitech.model.Paciente; // Modelo de la entidad Paciente
import com.utp.clinitech.model.Usuario; // Modelo de la entidad Usuario
import com.utp.clinitech.model.enums.EstadoCita; // Enum para los estados posibles de una cita
import com.utp.clinitech.model.enums.RolUsuario; // Enum para los roles posibles de un usuario
import com.utp.clinitech.util.ColaPrioridad; // Clase para manejar la cola de prioridad de citas

@Service // Marca la clase como un servicio de Spring
@Transactional(readOnly = true)
public class CitaService {
  private static final ZoneId CLINIC_ZONE = ZoneId.of("America/Lima"); // Define la zona horaria de la clínica
  private final CitaDAO citas; // Acceso a datos para la entidad Cita
  private final PacienteDAO pacientes; // Acceso a datos para la entidad Paciente
  private final MedicoService medicos; // Servicio para manejar la lógica de negocio relacionada con los médicos
  private final CurrentUserService currentUser; // Servicio para obtener información del usuario actual

  // Constructor de la clase CitaService que inyecta las dependencias necesarias
  public CitaService(CitaDAO citas, PacienteDAO pacientes, MedicoService medicos, CurrentUserService currentUser) {
    this.citas = citas;
    this.pacientes = pacientes;
    this.medicos = medicos;
    this.currentUser = currentUser;
  }

  @Transactional
  // Crea una nueva cita médica en el sistema
  public CitaResponse crear(CitaRequest request) {
    Usuario actor = currentUser.required(); // Obtiene el usuario actual y lanza una excepción si no está autenticado. 
    Long pacienteId = request.pacienteId(); // Obtiene el ID del paciente desde la solicitud de creación de cita. 
    if (actor.getRol() == RolUsuario.PACIENTE) { // Si el usuario actual es un paciente, se realizan validaciones adicionales.
      if (actor.getPaciente() == null)
        throw new ApiException(HttpStatus.FORBIDDEN, "La cuenta no está vinculada a un paciente");
      if (pacienteId != null && !pacienteId.equals(actor.getPaciente().getId()))
        throw new ApiException(HttpStatus.FORBIDDEN, "No puedes agendar para otro paciente");
      pacienteId = actor.getPaciente().getId(); // Se asigna el ID del paciente del usuario actual para garantizar que solo pueda agendar citas para sí mismo.
    } else
      currentUser.requireRole(actor, RolUsuario.ADMIN, RolUsuario.RECEPCIONISTA); // Si el usuario no es un paciente, se requiere que tenga el rol de ADMIN o RECEPCIONISTA para continuar.
    if (pacienteId == null)
      throw new ApiException(HttpStatus.BAD_REQUEST, "Debe indicar un paciente");
    Medico medico = medicos.entidad(request.medicoId());
    if (citas.existsByMedicoIdAndFechaHora(medico.getId(), request.fechaHora())) // Verifica si ya existe una cita para el médico en la fecha y hora especificadas. Si es así, lanza una excepción indicando que el horario seleccionado ya no está disponible.
      throw new ApiException(HttpStatus.CONFLICT, "El horario seleccionado ya no está disponible");
    Cita cita = new Cita(pacientesEntidad(pacienteId), medico, request.fechaHora(), request.prioridad(),
        request.motivoConsulta().trim()); // Crea una nueva instancia de la entidad Cita con los datos proporcionados en la solicitud.
    return ApiMapper.cita(citas.save(cita));
  }

  // Lista las citas médicas según los parámetros proporcionados y el rol del usuario actual
  public List<CitaResponse> listar(Long pacienteId, Long medicoId) {
    Usuario actor = currentUser.required(); // Obtiene el usuario actual y lanza una excepción si no está autenticado.
    List<Cita> result; // Lista de citas que se llenará según el rol del usuario y los parámetros proporcionados.
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
      result = citas.findAll(); // Si no se proporcionan ni pacienteId ni medicoId, se obtienen todas las citas disponibles en el sistema.
    return result.stream().map(ApiMapper::cita).toList();
  }

  // Obtiene la disponibilidad de horarios para un médico específico en una fecha determinada
  public List<OffsetDateTime> disponibilidad(Long medicoId, LocalDate fecha) {
    currentUser.required(); // Obtiene el usuario actual y lanza una excepción si no está autenticado.
    Medico medico = medicos.entidad(medicoId);
    OffsetDateTime inicio = fecha.atStartOfDay(CLINIC_ZONE).toOffsetDateTime();
    OffsetDateTime fin = fecha.plusDays(1).atStartOfDay(CLINIC_ZONE).toOffsetDateTime();
    List<OffsetDateTime> ocupados = citas.findByMedicoIdAndFechaHoraBetweenOrderByFechaHoraAsc(medicoId, inicio, fin)
        .stream()
        .filter(c -> c.getEstado() != EstadoCita.CANCELADA).map(Cita::getFechaHora).toList(); 

    int[] rango = obtenerRangoSlots(medico.getHorarioAtencion()); 
    return IntStream.range(rango[0], rango[1])
        .mapToObj(i -> fecha.atTime(i / 2, i % 2 * 30).atZone(CLINIC_ZONE).toOffsetDateTime())
        .filter(slot -> slot.isAfter(OffsetDateTime.now()) && !ocupados.contains(slot)).toList();
  }

  // Obtiene la cola de atención de citas para un médico específico en una fecha determinada, ordenada por prioridad y hora de cita.
  public List<CitaResponse> colaAtencion(Long medicoId, LocalDate fecha) {
    validarAccesoMedicoOAdmin(medicoId);
    ColaPrioridad cola = construirColaDelDia(medicoId, fecha);
    return cola.ordenadas().stream().map(ApiMapper::cita).toList();
  }

  // Obtiene la siguiente cita en la cola de atención para un médico específico en una fecha determinada.
  public CitaResponse siguienteEnCola(Long medicoId, LocalDate fecha) {
    validarAccesoMedicoOAdmin(medicoId);
    ColaPrioridad cola = construirColaDelDia(medicoId, fecha);
    Cita siguiente = cola.siguiente()
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No hay citas pendientes en la cola de atención"));
    return ApiMapper.cita(siguiente);
  }

  // Construye la cola de atención de citas para un médico específico en una fecha determinada, filtrando las citas pendientes, confirmadas o reprogramadas.
  private ColaPrioridad construirColaDelDia(Long medicoId, LocalDate fecha) {
    LocalDate target = fecha != null ? fecha : LocalDate.now(CLINIC_ZONE);
    OffsetDateTime inicio = target.atStartOfDay(CLINIC_ZONE).toOffsetDateTime();
    OffsetDateTime fin = target.plusDays(1).atStartOfDay(CLINIC_ZONE).toOffsetDateTime();

    List<Cita> delDia = citas.findByMedicoIdAndFechaHoraBetweenOrderByFechaHoraAsc(medicoId, inicio, fin).stream()
        .filter(c -> c.getEstado() == EstadoCita.PENDIENTE || c.getEstado() == EstadoCita.CONFIRMADA
            || c.getEstado() == EstadoCita.REPROGRAMADA)
        .toList();

    ColaPrioridad cola = new ColaPrioridad();
    for (Cita c : delDia) {
      cola.encolar(c);
    }
    return cola;
  }

  // Valida que el usuario actual tenga acceso a la cola de atención del médico especificado, permitiendo el acceso solo a administradores, recepcionistas o al propio médico.
  private void validarAccesoMedicoOAdmin(Long medicoId) {
    Usuario actor = currentUser.required();
    if (actor.getRol() == RolUsuario.ADMIN || actor.getRol() == RolUsuario.RECEPCIONISTA)
      return;
    if (actor.getRol() == RolUsuario.MEDICO && actor.getMedico() != null && actor.getMedico().getId().equals(medicoId))
      return;
    throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para acceder a la cola de atención de este médico");
  }

  // Obtiene el rango de horarios disponibles para el médico según su horario de atención.
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
  // Reprograma una cita existente, validando que el usuario tenga permiso para hacerlo y que el nuevo horario esté disponible.
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
  // Cambia el estado de una cita existente, validando que el usuario tenga permiso para hacerlo y que el cambio de estado sea válido según las reglas del sistema.
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
      currentUser.requireRole(actor, RolUsuario.ADMIN, RolUsuario.RECEPCIONISTA);
    if (cita.getEstado() == EstadoCita.ATENDIDA)
      throw new ApiException(HttpStatus.CONFLICT, "Una cita atendida no puede cambiar de estado");
    cita.cambiarEstado(request.estado());
    return ApiMapper.cita(cita);
  }

  // Obtiene la entidad Cita correspondiente al ID proporcionado, lanzando una excepción si no se encuentra.
  public Cita obtenerEntidad(Long id) {
    return citas.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
  }

  // Obtiene la entidad Paciente correspondiente al ID proporcionado, asegurándose de que esté activa y lanzando una excepción si no se encuentra.
  private Paciente pacientesEntidad(Long id) {
    return pacientes.findById(id).filter(Paciente::isActivo)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
  }

  // Valida que el usuario actual tenga permiso para modificar la cita, permitiendo el acceso solo a administradores, recepcionistas o al propio paciente asociado a la cita.
  private void validarPacienteOAdmin(Usuario actor, Cita cita) {
    if (actor.getRol() == RolUsuario.ADMIN || actor.getRol() == RolUsuario.RECEPCIONISTA)
      return;
    if (actor.getRol() == RolUsuario.PACIENTE && actor.getPaciente() != null
        && actor.getPaciente().getId().equals(cita.getPaciente().getId()))
      return;
    throw new ApiException(HttpStatus.FORBIDDEN, "No puedes modificar esta cita");
  }
}
