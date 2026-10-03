package com.utp.clinitech.service;

import java.util.List; // Usado para manejar colecciones de médicos por especialidad.
import org.springframework.data.domain.PageRequest; // Usado para estructurar solicitudes de página con ordenamiento.
import org.springframework.data.domain.Sort; // Usado para indicar ordenamiento por apellidos.
import org.springframework.http.HttpStatus; // Usado para devolver códigos de estado en errores de negocio.
import org.springframework.stereotype.Service; // Usado para marcar la clase como servicio de Spring.
import org.springframework.transaction.annotation.Transactional; // Usado para delimitar transacciones de base de datos.
import com.utp.clinitech.dao.MedicoDAO; // Usado para ejecutar consultas y operaciones de persistencia sobre médicos.
import com.utp.clinitech.dto.MedicoRequest; // Usado para recibir datos de registro o edición del médico.
import com.utp.clinitech.dto.MedicoResponse; // Usado para devolver los datos de los médicos al cliente.
import com.utp.clinitech.dto.PageResponse; // Usado para encapsular listados paginados de médicos.
import com.utp.clinitech.exception.ApiException; // Usado para lanzar excepción si el médico no es encontrado.
import com.utp.clinitech.model.Medico; // Usado para representar y manipular la entidad Medico.
import com.utp.clinitech.model.enums.RolUsuario; // Usado para validar permisos exclusivos de ADMINISTRADOR.

// Servicio de negocio para la gestión del personal médico del hospital.
@Service
@Transactional(readOnly = true)
public class MedicoService {
  private final MedicoDAO medicos;
  private final EspecialidadService especialidades;
  private final CurrentUserService currentUser;

  public MedicoService(MedicoDAO medicos, EspecialidadService especialidades, CurrentUserService currentUser) {
    this.medicos = medicos;
    this.especialidades = especialidades;
    this.currentUser = currentUser;
  }

  // Retorna la lista de médicos activos pertenecientes a una especialidad médica específica.
  public List<MedicoResponse> porEspecialidad(Long especialidadId) {
    currentUser.required();
    return medicos.findByActivoTrueAndEspecialidadIdOrderByApellidosAsc(especialidadId).stream().map(ApiMapper::medico)
        .toList();
  }

  // Listado paginado de médicos activos con soporte para búsqueda por nombres, apellidos o CMP.
  public PageResponse<MedicoResponse> listar(String q, int page, int size) {
    currentUser.required();
    return PageResponse.from(medicos
        .buscarActivos(q == null ? "" : q.trim(), PageRequest.of(page, Math.min(size, 100), Sort.by("apellidos")))
        .map(ApiMapper::medico));
  }

  // Registra un nuevo médico y lo vincula con su especialidad correspondiente.
  @Transactional
  public MedicoResponse crear(MedicoRequest request) {
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN);
    Medico medico = new Medico();
    medico.actualizar(request.nombres().trim(), request.apellidos().trim(), request.cmp().toUpperCase(),
        especialidades.entidad(request.especialidadId()), request.telefono(), request.correo().trim().toLowerCase(),
        request.horarioAtencion().trim());
    return ApiMapper.medico(medicos.save(medico));
  }

  // Actualiza los datos profesionales y de contacto de un médico.
  @Transactional
  public MedicoResponse actualizar(Long id, MedicoRequest request) {
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN);
    Medico medico = entidad(id);
    medico.actualizar(request.nombres().trim(), request.apellidos().trim(), request.cmp().toUpperCase(),
        especialidades.entidad(request.especialidadId()), request.telefono(), request.correo().trim().toLowerCase(),
        request.horarioAtencion().trim());
    return ApiMapper.medico(medico);
  }

  // Desactiva lógicamente a un médico en el sistema.
  @Transactional
  public void desactivar(Long id) {
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN);
    entidad(id).cambiarEstado(false);
  }

  // Recupera la entidad Medico activa por ID o lanza 404 NOT_FOUND.
  public Medico entidad(Long id) {
    return medicos.findById(id).filter(Medico::isActivo)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Médico no encontrado"));
  }
}
