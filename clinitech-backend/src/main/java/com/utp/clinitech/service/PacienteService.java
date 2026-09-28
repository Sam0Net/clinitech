package com.utp.clinitech.service;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.utp.clinitech.dao.*;
import com.utp.clinitech.dto.*;
import com.utp.clinitech.exception.ApiException;
import com.utp.clinitech.model.Paciente;
import com.utp.clinitech.model.Usuario;
import com.utp.clinitech.model.enums.RolUsuario;

import jakarta.annotation.PostConstruct;
import java.util.List;
import com.utp.clinitech.util.ArbolBusquedaPacientes;

@Service
@Transactional(readOnly = true)
public class PacienteService {
  private final PacienteDAO pacientes; private final CitaDAO citas; private final CurrentUserService currentUser;
  private final ArbolBusquedaPacientes arbolPacientes = new ArbolBusquedaPacientes();

  public PacienteService(PacienteDAO pacientes, CitaDAO citas, CurrentUserService currentUser) {
    this.pacientes = pacientes; this.citas = citas; this.currentUser = currentUser;
  }

  @PostConstruct
  public void inicializarArbol() {
    pacientes.findAll().stream().filter(Paciente::isActivo).forEach(arbolPacientes::insertar);
  }

  public PacienteResponse buscarPorDniEnArbol(String dni) {
    currentUser.required();
    return arbolPacientes.buscarPorDni(dni)
        .filter(Paciente::isActivo)
        .map(ApiMapper::paciente)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Paciente no encontrado en el índice del árbol"));
  }

  public List<PacienteResponse> listarEnOrdenArbol() {
    currentUser.required();
    return arbolPacientes.enOrden().stream()
        .filter(Paciente::isActivo)
        .map(ApiMapper::paciente)
        .toList();
  }

  public PageResponse<PacienteResponse> listar(String q, int page, int size) {
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN);
    Page<Paciente> result = pacientes.buscarActivos(q == null ? "" : q.trim(), PageRequest.of(page, Math.min(size, 100), Sort.by("apellidos").ascending()));
    return PageResponse.from(result.map(ApiMapper::paciente));
  }
  public PacienteResponse obtener(Long id) {
    Usuario actor = currentUser.required();
    Paciente paciente = buscar(id);
    boolean own = actor.getRol() == RolUsuario.PACIENTE && actor.getPaciente() != null && actor.getPaciente().getId().equals(id);
    boolean treatingDoctor = actor.getRol() == RolUsuario.MEDICO && actor.getMedico() != null && citas.existsByMedicoIdAndPacienteId(actor.getMedico().getId(), id);
    if (!own && !treatingDoctor && actor.getRol() != RolUsuario.ADMIN) throw new ApiException(HttpStatus.FORBIDDEN, "No puedes acceder a este paciente");
    return ApiMapper.paciente(paciente);
  }
  @Transactional
  public PacienteResponse crear(PacienteRequest request) {
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN);
    if (pacientes.findByDni(request.dni()).isPresent()) throw new ApiException(HttpStatus.CONFLICT, "El DNI ya está registrado");
    Paciente paciente = new Paciente();
    paciente.actualizar(request.dni(), request.nombres().trim(), request.apellidos().trim(), request.fechaNacimiento(), request.telefono(), request.correo().trim().toLowerCase(), request.direccion(), request.alergias(), request.historialMedico());
    Paciente guardado = pacientes.save(paciente);
    arbolPacientes.insertar(guardado);
    return ApiMapper.paciente(guardado);
  }
  @Transactional
  public PacienteResponse actualizar(Long id, PacienteRequest request) {
    Usuario actor = currentUser.required();
    boolean own = actor.getRol() == RolUsuario.PACIENTE && actor.getPaciente() != null && actor.getPaciente().getId().equals(id);
    if (!own && actor.getRol() != RolUsuario.ADMIN) throw new ApiException(HttpStatus.FORBIDDEN, "No puedes modificar este paciente");
    Paciente paciente = buscar(id);
    paciente.actualizar(request.dni(), request.nombres().trim(), request.apellidos().trim(), request.fechaNacimiento(), request.telefono(), request.correo().trim().toLowerCase(), request.direccion(), request.alergias(), request.historialMedico());
    arbolPacientes.insertar(paciente);
    return ApiMapper.paciente(paciente);
  }
  @Transactional
  public void desactivar(Long id) {
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN);
    Paciente paciente = buscar(id);
    paciente.cambiarEstado(false);
    arbolPacientes.eliminar(paciente.getDni());
  }
  @Transactional
  public Paciente registrarNuevoPaciente(Paciente paciente) {
    if (pacientes.findByDni(paciente.getDni()).isPresent()) {
      throw new ApiException(HttpStatus.CONFLICT, "El DNI ya está registrado");
    }
    Paciente guardado = pacientes.save(paciente);
    arbolPacientes.insertar(guardado);
    return guardado;
  }
  private Paciente buscar(Long id) { return pacientes.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Paciente no encontrado")); }
}
