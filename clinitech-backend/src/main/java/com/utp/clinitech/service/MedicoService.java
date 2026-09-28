package com.utp.clinitech.service;

import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.utp.clinitech.dao.MedicoDAO;
import com.utp.clinitech.dto.*;
import com.utp.clinitech.exception.ApiException;
import com.utp.clinitech.model.*;
import com.utp.clinitech.model.enums.RolUsuario;

@Service
@Transactional(readOnly = true)
public class MedicoService {
  private final MedicoDAO medicos; private final EspecialidadService especialidades; private final CurrentUserService currentUser;
  public MedicoService(MedicoDAO medicos, EspecialidadService especialidades, CurrentUserService currentUser) { this.medicos = medicos; this.especialidades = especialidades; this.currentUser = currentUser; }
  public List<MedicoResponse> porEspecialidad(Long especialidadId) { currentUser.required(); return medicos.findByActivoTrueAndEspecialidadIdOrderByApellidosAsc(especialidadId).stream().map(ApiMapper::medico).toList(); }
  public PageResponse<MedicoResponse> listar(String q, int page, int size) { currentUser.required(); return PageResponse.from(medicos.buscarActivos(q == null ? "" : q.trim(), PageRequest.of(page, Math.min(size, 100), Sort.by("apellidos"))).map(ApiMapper::medico)); }
  @Transactional public MedicoResponse crear(MedicoRequest request) { currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN); Medico medico = new Medico(); medico.actualizar(request.nombres().trim(), request.apellidos().trim(), request.cmp().toUpperCase(), especialidades.entidad(request.especialidadId()), request.telefono(), request.correo().trim().toLowerCase(), request.horarioAtencion().trim()); return ApiMapper.medico(medicos.save(medico)); }
  @Transactional public MedicoResponse actualizar(Long id, MedicoRequest request) { currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN); Medico medico = entidad(id); medico.actualizar(request.nombres().trim(), request.apellidos().trim(), request.cmp().toUpperCase(), especialidades.entidad(request.especialidadId()), request.telefono(), request.correo().trim().toLowerCase(), request.horarioAtencion().trim()); return ApiMapper.medico(medico); }
  @Transactional public void desactivar(Long id) { currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN); entidad(id).cambiarEstado(false); }
  public Medico entidad(Long id) { return medicos.findById(id).filter(Medico::isActivo).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Médico no encontrado")); }
}
