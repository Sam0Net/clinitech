package com.utp.clinitech.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.utp.clinitech.dao.EspecialidadDAO;
import com.utp.clinitech.dto.*;
import com.utp.clinitech.exception.ApiException;
import com.utp.clinitech.model.Especialidad;
import com.utp.clinitech.model.enums.RolUsuario;

import jakarta.annotation.PostConstruct;
import java.util.Set;
import com.utp.clinitech.util.GrafoEspecialidades;

@Service
@Transactional(readOnly = true)
public class EspecialidadService {
  private final EspecialidadDAO especialidades; private final CurrentUserService currentUser;
  private final GrafoEspecialidades grafo = new GrafoEspecialidades();

  public EspecialidadService(EspecialidadDAO especialidades, CurrentUserService currentUser) {
    this.especialidades = especialidades; this.currentUser = currentUser;
  }

  @PostConstruct
  public void inicializarGrafo() {
    List<Especialidad> lista = especialidades.findByActivoTrueOrderByNombreAsc();
    for (Especialidad e : lista) {
      grafo.agregarEspecialidad(e.getId());
    }
    // Conectar Medicina General con las demás especialidades si existen
    Especialidad general = lista.stream().filter(e -> e.getNombre().toLowerCase().contains("general")).findFirst().orElse(null);
    if (general != null) {
      for (Especialidad e : lista) {
        if (!e.getId().equals(general.getId())) {
          grafo.relacionar(general.getId(), e.getId());
        }
      }
    }
  }

  public List<EspecialidadResponse> listar() {
    currentUser.required();
    return especialidades.findByActivoTrueOrderByNombreAsc().stream().map(ApiMapper::especialidad).toList();
  }

  public List<EspecialidadResponse> relacionadas(Long id) {
    currentUser.required();
    entidad(id);
    Set<Long> idsRelacionadas = grafo.relacionadasCon(id);
    return especialidades.findAllById(idsRelacionadas).stream()
        .filter(Especialidad::isActivo)
        .map(ApiMapper::especialidad)
        .toList();
  }

  public List<EspecialidadResponse> rutaInterconsulta(Long origenId, Long destinoId) {
    currentUser.required();
    entidad(origenId);
    entidad(destinoId);
    List<Long> rutaIds = grafo.rutaMasCorta(origenId, destinoId);
    if (rutaIds.isEmpty()) {
      return List.of();
    }
    // Mantener el orden exacto de la ruta calculada por BFS
    return rutaIds.stream()
        .map(this::entidad)
        .map(ApiMapper::especialidad)
        .toList();
  }

  @Transactional
  public EspecialidadResponse crear(EspecialidadRequest request) {
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN);
    Especialidad guardada = especialidades.save(new Especialidad(request.nombre().trim(), request.descripcion()));
    grafo.agregarEspecialidad(guardada.getId());
    return ApiMapper.especialidad(guardada);
  }

  public Especialidad entidad(Long id) {
    return especialidades.findById(id).filter(Especialidad::isActivo).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Especialidad no encontrada"));
  }
}
