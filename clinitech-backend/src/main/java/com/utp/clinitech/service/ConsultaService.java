package com.utp.clinitech.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.utp.clinitech.dao.CitaDAO;
import com.utp.clinitech.dao.ConsultaDAO;
import com.utp.clinitech.dto.ConsultaRequest;
import com.utp.clinitech.dto.ConsultaResponse;
import com.utp.clinitech.exception.ApiException;
import com.utp.clinitech.model.Cita;
import com.utp.clinitech.model.Consulta;
import com.utp.clinitech.model.Usuario;
import com.utp.clinitech.model.enums.EstadoCita;
import com.utp.clinitech.model.enums.RolUsuario;

@Service
@Transactional(readOnly = true)
public class ConsultaService {
  private final ConsultaDAO consultas; private final CitaDAO citas; private final CurrentUserService currentUser;
  public ConsultaService(ConsultaDAO consultas, CitaDAO citas, CurrentUserService currentUser) { this.consultas = consultas; this.citas = citas; this.currentUser = currentUser; }
  @Transactional
  public ConsultaResponse registrar(ConsultaRequest request) {
    Usuario actor = currentUser.required(); currentUser.requireRole(actor, RolUsuario.MEDICO);
    if (actor.getMedico() == null) throw new ApiException(HttpStatus.FORBIDDEN, "La cuenta no está vinculada a un médico");
    Cita cita = citas.findById(request.citaId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
    if (!cita.getMedico().getId().equals(actor.getMedico().getId())) throw new ApiException(HttpStatus.FORBIDDEN, "No puedes registrar una consulta para otra cita");
    if (consultas.existsByCitaId(cita.getId())) throw new ApiException(HttpStatus.CONFLICT, "La cita ya tiene una consulta registrada");
    if (cita.getEstado() == EstadoCita.CANCELADA) throw new ApiException(HttpStatus.CONFLICT, "No se puede atender una cita cancelada");
    cita.cambiarEstado(EstadoCita.ATENDIDA);
    return ApiMapper.consulta(consultas.save(new Consulta(cita, request.diagnostico().trim(), request.tratamiento().trim(), request.observaciones())));
  }
  public List<ConsultaResponse> historial(Long pacienteId) {
    Usuario actor = currentUser.required();
    boolean own = actor.getRol() == RolUsuario.PACIENTE && actor.getPaciente() != null && actor.getPaciente().getId().equals(pacienteId);
    boolean treats = actor.getRol() == RolUsuario.MEDICO && actor.getMedico() != null && citas.existsByMedicoIdAndPacienteId(actor.getMedico().getId(), pacienteId);
    if (!own && !treats && actor.getRol() != RolUsuario.ADMIN) throw new ApiException(HttpStatus.FORBIDDEN, "No puedes acceder a este historial");
    return consultas.findByCitaPacienteIdOrderByFechaAtencionDesc(pacienteId).stream().map(ApiMapper::consulta).toList();
  }
}
