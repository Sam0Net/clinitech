package com.utp.clinitech.service;

import org.springframework.data.domain.Page; // Usado para la paginación de resultados.
import org.springframework.data.domain.PageRequest; // Usado para crear solicitudes de página con tamaño y número de página específicos.
import org.springframework.data.domain.Sort; // Usado para especificar el ordenamiento de los resultados.
import org.springframework.http.HttpStatus; // Usado para representar códigos de estado HTTP en las respuestas de la API.
import org.springframework.stereotype.Service; // Usado para marcar la clase como un servicio de Spring.
import org.springframework.transaction.annotation.Transactional; // Usado para manejar transacciones de base de datos.
import com.utp.clinitech.dao.CitaDAO; // Usado para acceder a los datos de las citas médicas.
import com.utp.clinitech.dao.PacienteDAO; // Usado para acceder a los datos de los pacientes.
import com.utp.clinitech.dto.PacienteRequest; // Usado para recibir datos de solicitud relacionados con pacientes.
import com.utp.clinitech.dto.PacienteResponse; // Usado para enviar datos de respuesta relacionados con pacientes.
import com.utp.clinitech.dto.PageResponse; // Usado para enviar respuestas paginadas de la API.
import com.utp.clinitech.exception.ApiException; // Usado para manejar excepciones específicas de la API.
import com.utp.clinitech.model.Paciente; // Usado para representar la entidad de paciente en la base de datos.
import com.utp.clinitech.model.Usuario; // Usado para representar la entidad de usuario en la base de datos.
import com.utp.clinitech.model.enums.RolUsuario; // Usado para representar los roles de usuario en la aplicación.
import jakarta.annotation.PostConstruct; // Usado para ejecutar un método después de que la clase haya sido construida y sus dependencias inyectadas.
import java.util.List; // Usado para trabajar con listas de objetos.
import com.utp.clinitech.util.ArbolBusquedaPacientes; // Usado para manejar un árbol de búsqueda de pacientes para búsquedas rápidas.

@Service
@Transactional(readOnly = true)
public class PacienteService {
  private final PacienteDAO pacientes;
  private final CitaDAO citas;
  private final CurrentUserService currentUser;
  private final ArbolBusquedaPacientes arbolPacientes = new ArbolBusquedaPacientes();

  public PacienteService(PacienteDAO pacientes, CitaDAO citas, CurrentUserService currentUser) {
    this.pacientes = pacientes;
    this.citas = citas;
    this.currentUser = currentUser;
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
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN, RolUsuario.RECEPCIONISTA);
    Page<Paciente> result = pacientes.buscarActivos(q == null ? "" : q.trim(),
        PageRequest.of(page, Math.min(size, 100), Sort.by("apellidos").ascending()));
    return PageResponse.from(result.map(ApiMapper::paciente));
  }

  public PacienteResponse obtener(Long id) {
    Usuario actor = currentUser.required();
    Paciente paciente = buscar(id);
    boolean own = actor.getRol() == RolUsuario.PACIENTE && actor.getPaciente() != null
        && actor.getPaciente().getId().equals(id);
    boolean treatingDoctor = actor.getRol() == RolUsuario.MEDICO && actor.getMedico() != null
        && citas.existsByMedicoIdAndPacienteId(actor.getMedico().getId(), id);
    if (!own && !treatingDoctor && actor.getRol() != RolUsuario.ADMIN && actor.getRol() != RolUsuario.RECEPCIONISTA)
      throw new ApiException(HttpStatus.FORBIDDEN, "No puedes acceder a este paciente");
    return ApiMapper.paciente(paciente);
  }

  @Transactional
  public PacienteResponse crear(PacienteRequest request) {
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN, RolUsuario.RECEPCIONISTA);
    if (pacientes.findByDni(request.dni()).isPresent())
      throw new ApiException(HttpStatus.CONFLICT, "El DNI ya está registrado");
    Paciente paciente = new Paciente();
    paciente.actualizar(request.dni(), request.nombres().trim(), request.apellidos().trim(), request.fechaNacimiento(),
        request.telefono(), request.correo().trim().toLowerCase(), request.direccion(), request.alergias(),
        request.historialMedico());
    Paciente guardado = pacientes.save(paciente);
    arbolPacientes.insertar(guardado);
    return ApiMapper.paciente(guardado);
  }

  @Transactional
  public PacienteResponse actualizar(Long id, PacienteRequest request) {
    Usuario actor = currentUser.required();
    boolean own = actor.getRol() == RolUsuario.PACIENTE && actor.getPaciente() != null
        && actor.getPaciente().getId().equals(id);
    if (!own && actor.getRol() != RolUsuario.ADMIN && actor.getRol() != RolUsuario.RECEPCIONISTA)
      throw new ApiException(HttpStatus.FORBIDDEN, "No puedes modificar este paciente");
    Paciente paciente = buscar(id);
    paciente.actualizar(request.dni(), request.nombres().trim(), request.apellidos().trim(), request.fechaNacimiento(),
        request.telefono(), request.correo().trim().toLowerCase(), request.direccion(), request.alergias(),
        request.historialMedico());
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

  private Paciente buscar(Long id) {
    return pacientes.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
  }
}
