package com.utp.clinitech.service;

import java.util.List; // Importa la clase List para manejar colecciones de elementos ordenados.
import java.util.Set; // Importa la clase Set para manejar colecciones de elementos únicos.
import org.springframework.http.HttpStatus; // Importa la clase HttpStatus para manejar códigos de estado HTTP en las respuestas.
import org.springframework.stereotype.Service; // Marca la clase como un servicio de Spring.
import org.springframework.transaction.annotation.Transactional; // Gestiona las transacciones con la base de datos.
import com.utp.clinitech.dao.EspecialidadDAO; // Importa la interfaz DAO para acceder a la base de datos de especialidades.
import com.utp.clinitech.dto.EspecialidadRequest; // Envia y recibe datos en JSON sin exponer la entidad de la bd.
import com.utp.clinitech.dto.EspecialidadResponse; // Envia y recibe datos en JSON sin exponer la entidad de la bd.
import com.utp.clinitech.exception.ApiException; // Importa la clase ApiException para manejar errores de la API de manera uniforme.
import com.utp.clinitech.model.Especialidad; // Importa la clase Especialidad que representa la entidad de especialidades en la bd.
import com.utp.clinitech.model.enums.RolUsuario; // Importa la enumeración RolUsuario para manejar roles de usuario en la aplicación.
import com.utp.clinitech.util.GrafoEspecialidades; // Importa la clase GrafoEspecialidades para manejar relaciones entre especialidades.
import jakarta.annotation.PostConstruct; // Ejecuta un método después de la construcción del bean, útil para inicializar datos.

@Service // Anotación que indica que es un servicio de Spring.
@Transactional(readOnly = true) // Solo lectura, no modifica la base de datos.
public class EspecialidadService {
  // Dependencias inyectadas a través del constructor.
  private final EspecialidadDAO especialidades;
  private final CurrentUserService currentUser;
  private final GrafoEspecialidades grafo = new GrafoEspecialidades();

  // Constructor que recibe las dependencias necesarias para el servicio.
  public EspecialidadService(EspecialidadDAO especialidades, CurrentUserService currentUser) {
    this.especialidades = especialidades;
    this.currentUser = currentUser;
  }

  @PostConstruct
  public void inicializarGrafo() {
    List<Especialidad> lista = especialidades.findByActivoTrueOrderByNombreAsc(); // Obtiene las especialidades activas
                                                                                  // de la bd y las ordena por nombre.
    for (Especialidad e : lista) {
      grafo.agregarEspecialidad(e.getId());
    }
    // Conectar Medicina General con las demás especialidades para que funcione como
    // nodo central de triaje y derivación.
    Especialidad general = lista.stream().filter(e -> e.getNombre().toLowerCase().contains("general")).findFirst()
        .orElse(null);
    if (general != null) {
      for (Especialidad e : lista) {
        if (!e.getId().equals(general.getId())) {
          grafo.relacionar(general.getId(), e.getId());
        }
      }
    }
  }

  public List<EspecialidadResponse> listar() { // Método para listar todas las especialidades activas.
    currentUser.required();
    return especialidades.findByActivoTrueOrderByNombreAsc().stream().map(ApiMapper::especialidad).toList();
  }

  public List<EspecialidadResponse> relacionadas(Long id) { // Obtener las especialidades relacionadas.
    currentUser.required(); // Verifica que el usuario esté autenticado.
    entidad(id); // Verifica que la especialidad con el ID proporcionado exista y esté activa.
    Set<Long> idsRelacionadas = grafo.relacionadasCon(id); // Obtiene los IDs de las especialidades relacionadas.
    return especialidades.findAllById(idsRelacionadas).stream() // Convierte en DTOs y filtra solo las activas.
        .filter(Especialidad::isActivo)
        .map(ApiMapper::especialidad)
        .toList();
  }

  public List<EspecialidadResponse> rutaInterconsulta(Long origenId, Long destinoId) { // Obtiene ruta más corta.
    currentUser.required(); // Verifica que el usuario esté autenticado.
    entidad(origenId); // Verifica que la especialidad de origen exista y esté activa.
    entidad(destinoId); // Verifica que la especialidad de destino exista y esté activa.
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
  public EspecialidadResponse crear(EspecialidadRequest request) { // Método para crear una nueva especialidad.
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN); // Verifica autenticado como ADMIN.
    Especialidad guardada = especialidades.save(new Especialidad(request.nombre().trim(), request.descripcion())); // Guarda la nueva especialidad en la base de datos.
    grafo.agregarEspecialidad(guardada.getId()); // Agrega la nueva especialidad al grafo de relaciones.
    return ApiMapper.especialidad(guardada); // Convierte la entidad guardada en un DTO de respuesta.
  }

  public Especialidad entidad(Long id) { // Obtiene la entidad por ID. 
    return especialidades.findById(id).filter(Especialidad::isActivo) // Filtra solo las especialidades activas.
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Especialidad no encontrada")); // Lanza una excepción si no se encuentra la especialidad.
  }
}
