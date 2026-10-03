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

@Service // Marca la clase como componente de servicio de lógica de negocio en Spring.
@Transactional(readOnly = true) // Por defecto, todas las operaciones de lectura se ejecutan en modo de solo lectura.
public class PacienteService {
  private final PacienteDAO pacientes; // DAO para operaciones de persistencia sobre la tabla de pacientes.
  private final CitaDAO citas; // DAO para verificar la relación entre médico y paciente a través de sus citas.
  private final CurrentUserService currentUser; // Servicio utilitario para validar usuario autenticado y sus permisos.
  private final ArbolBusquedaPacientes arbolPacientes = new ArbolBusquedaPacientes(); // Estructura ABB en memoria para búsquedas O(log n) y listado ordenado.

  // Constructor que recibe las dependencias requeridas mediante inyección de Spring.
  public PacienteService(PacienteDAO pacientes, CitaDAO citas, CurrentUserService currentUser) {
    this.pacientes = pacientes;
    this.citas = citas;
    this.currentUser = currentUser;
  }

  // Se ejecuta tras iniciar el servicio: carga todos los pacientes activos de la BD al Árbol Binario de Búsqueda.
  @PostConstruct
  public void inicializarArbol() {
    pacientes.findAll().stream().filter(Paciente::isActivo).forEach(arbolPacientes::insertar); // Inserta cada paciente activo en el árbol.
  }

  // Búsqueda rápida en memoria O(log n) por DNI usando el Árbol Binario de Búsqueda.
  public PacienteResponse buscarPorDniEnArbol(String dni) {
    currentUser.required(); // Requiere sesión autenticada.
    return arbolPacientes.buscarPorDni(dni) // Busca el nodo correspondiente al DNI en el ABB.
        .filter(Paciente::isActivo) // Asegura que el paciente se encuentre en estado activo.
        .map(ApiMapper::paciente) // Transforma la entidad al DTO de respuesta.
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Paciente no encontrado en el índice del árbol"));
  }

  // Obtiene todos los pacientes ordenados ascendentemente mediante el recorrido In-Orden (Izquierda - Raíz - Derecha).
  public List<PacienteResponse> listarEnOrdenArbol() {
    currentUser.required(); // Requiere sesión autenticada.
    return arbolPacientes.enOrden().stream() // Ejecuta el recorrido in-order en tiempo O(n).
        .filter(Paciente::isActivo)
        .map(ApiMapper::paciente)
        .toList();
  }

  // Búsqueda y listado paginado en base de datos ordenado alfabéticamente por apellidos.
  public PageResponse<PacienteResponse> listar(String q, int page, int size) {
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN, RolUsuario.RECEPCIONISTA); // Exclusivo para Admin o Recepcionista.
    Page<Paciente> result = pacientes.buscarActivos(q == null ? "" : q.trim(),
        PageRequest.of(page, Math.min(size, 100), Sort.by("apellidos").ascending()));
    return PageResponse.from(result.map(ApiMapper::paciente)); // Retorna resultado encapsulado con metadatos de paginación.
  }

  // Obtiene la ficha de un paciente verificando estrictamente las políticas de privacidad y acceso.
  public PacienteResponse obtener(Long id) {
    Usuario actor = currentUser.required();
    Paciente paciente = buscar(id);
    boolean own = actor.getRol() == RolUsuario.PACIENTE && actor.getPaciente() != null
        && actor.getPaciente().getId().equals(id); // Verifica si es el propio paciente consultando su perfil.
    boolean treatingDoctor = actor.getRol() == RolUsuario.MEDICO && actor.getMedico() != null
        && citas.existsByMedicoIdAndPacienteId(actor.getMedico().getId(), id); // Verifica si es el médico tratante asignado.
    if (!own && !treatingDoctor && actor.getRol() != RolUsuario.ADMIN && actor.getRol() != RolUsuario.RECEPCIONISTA)
      throw new ApiException(HttpStatus.FORBIDDEN, "No puedes acceder a este paciente");
    return ApiMapper.paciente(paciente);
  }

  // Registra un nuevo paciente en base de datos y lo sincroniza de inmediato en el Árbol Binario de Búsqueda.
  @Transactional
  public PacienteResponse crear(PacienteRequest request) {
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN, RolUsuario.RECEPCIONISTA);
    if (pacientes.findByDni(request.dni()).isPresent())
      throw new ApiException(HttpStatus.CONFLICT, "El DNI ya está registrado");
    Paciente paciente = new Paciente();
    paciente.actualizar(request.dni(), request.nombres().trim(), request.apellidos().trim(), request.fechaNacimiento(),
        request.telefono(), request.correo().trim().toLowerCase(), request.direccion(), request.alergias(),
        request.historialMedico());
    Paciente guardado = pacientes.save(paciente); // Persiste en PostgreSQL.
    arbolPacientes.insertar(guardado); // Inserta el nuevo paciente en el Árbol en memoria O(log n).
    return ApiMapper.paciente(guardado);
  }

  // Actualiza los datos del paciente y sincroniza los cambios en el Árbol Binario.
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
    arbolPacientes.insertar(paciente); // Actualiza la referencia del paciente en el árbol.
    return ApiMapper.paciente(paciente);
  }

  // Da de baja lógica a un paciente en la base de datos y lo elimina del índice del Árbol Binario.
  @Transactional
  public void desactivar(Long id) {
    currentUser.requireRole(currentUser.required(), RolUsuario.ADMIN);
    Paciente paciente = buscar(id);
    paciente.cambiarEstado(false); // Inactiva lógicamente el registro.
    arbolPacientes.eliminar(paciente.getDni()); // Elimina el nodo del Árbol Binario O(log n).
  }

  // Registra un nuevo paciente desde el flujo de autoregistro público y lo indexa en el árbol.
  @Transactional
  public Paciente registrarNuevoPaciente(Paciente paciente) {
    if (pacientes.findByDni(paciente.getDni()).isPresent()) {
      throw new ApiException(HttpStatus.CONFLICT, "El DNI ya está registrado");
    }
    Paciente guardado = pacientes.save(paciente);
    arbolPacientes.insertar(guardado); // Sincroniza en el Árbol Binario.
    return guardado;
  }

  // Método auxiliar privado para buscar un paciente por ID en la BD o lanzar 404 NOT_FOUND.
  private Paciente buscar(Long id) {
    return pacientes.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
  }
}
