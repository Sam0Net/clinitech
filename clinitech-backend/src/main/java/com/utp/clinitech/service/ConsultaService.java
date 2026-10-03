package com.utp.clinitech.service;

import java.util.List; // Usado para retornar listas de consultas que conforman el historial médico.
import org.springframework.http.HttpStatus; // Usado para especificar códigos HTTP de error controlado.
import org.springframework.stereotype.Service; // Usado para marcar la clase como servicio de lógica de negocio.
import org.springframework.transaction.annotation.Transactional; // Usado para ejecutar las operaciones dentro de transacciones de base de datos.
import com.utp.clinitech.dao.CitaDAO; // Usado para consultar y actualizar el estado de la cita atendida.
import com.utp.clinitech.dao.ConsultaDAO; // Usado para comprobar existencia y guardar la consulta médica.
import com.utp.clinitech.dto.ConsultaRequest; // Usado para recibir el diagnóstico, tratamiento y notas.
import com.utp.clinitech.dto.ConsultaResponse; // Usado para enviar la consulta médica registrada al cliente.
import com.utp.clinitech.exception.ApiException; // Usado para lanzar errores de negocio (ej. cita ya atendida o no encontrada).
import com.utp.clinitech.model.Cita; // Usado para verificar y enlazar la cita con la consulta generada.
import com.utp.clinitech.model.Consulta; // Usado para instanciar la entidad Consulta.
import com.utp.clinitech.model.Usuario; // Usado para verificar los permisos del usuario en sesión.
import com.utp.clinitech.model.enums.EstadoCita; // Usado para cambiar el estado de la cita a ATENDIDA.
import com.utp.clinitech.model.enums.RolUsuario; // Usado para validar que solo médicos puedan registrar consultas.

// Servicio de negocio para la atención médica de citas y consulta de historias clínicas.
@Service
@Transactional(readOnly = true)
public class ConsultaService {
  private final ConsultaDAO consultas;
  private final CitaDAO citas;
  private final CurrentUserService currentUser;

  public ConsultaService(ConsultaDAO consultas, CitaDAO citas, CurrentUserService currentUser) {
    this.consultas = consultas;
    this.citas = citas;
    this.currentUser = currentUser;
  }

  // Registra la atención médica, receta y diagnóstico, cambiando el estado de la
  // cita a ATENDIDA.
  @Transactional
  public ConsultaResponse registrar(ConsultaRequest request) {
    Usuario actor = currentUser.required();
    currentUser.requireRole(actor, RolUsuario.MEDICO);
    if (actor.getMedico() == null)
      throw new ApiException(HttpStatus.FORBIDDEN, "La cuenta no está vinculada a un médico");
    Cita cita = citas.findById(request.citaId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
    if (!cita.getMedico().getId().equals(actor.getMedico().getId()))
      throw new ApiException(HttpStatus.FORBIDDEN, "No puedes registrar una consulta para otra cita");
    if (consultas.existsByCitaId(cita.getId()))
      throw new ApiException(HttpStatus.CONFLICT, "La cita ya tiene una consulta registrada");
    if (cita.getEstado() == EstadoCita.CANCELADA)
      throw new ApiException(HttpStatus.CONFLICT, "No se puede atender una cita cancelada");
    cita.cambiarEstado(EstadoCita.ATENDIDA); // Actualiza el estado de la cita.
    return ApiMapper.consulta(consultas
        .save(new Consulta(cita, request.diagnostico().trim(), request.tratamiento().trim(), request.observaciones())));
  }

  // Obtiene el historial clínico de un paciente asegurando que solo el paciente,
  // su médico o admin puedan verlo.
  public List<ConsultaResponse> historial(Long pacienteId) {
    Usuario actor = currentUser.required();
    boolean own = actor.getRol() == RolUsuario.PACIENTE && actor.getPaciente() != null
        && actor.getPaciente().getId().equals(pacienteId);
    boolean treats = actor.getRol() == RolUsuario.MEDICO && actor.getMedico() != null
        && citas.existsByMedicoIdAndPacienteId(actor.getMedico().getId(), pacienteId);
    if (!own && !treats && actor.getRol() != RolUsuario.ADMIN)
      throw new ApiException(HttpStatus.FORBIDDEN, "No puedes acceder a este historial");
    return consultas.findByCitaPacienteIdOrderByFechaAtencionDesc(pacienteId).stream().map(ApiMapper::consulta)
        .toList();
  }
}
