package com.utp.clinitech.controller;

import java.util.List; // Usado para manejar colecciones de médicos por especialidad.
import jakarta.validation.Valid; // Usado para validar los datos del médico antes de persistirlos.
import org.springframework.http.HttpStatus; // Usado para especificar códigos de estado HTTP en las respuestas.
import org.springframework.http.ResponseEntity; // Usado para encapsular el código 201 CREATED en la respuesta.
import org.springframework.web.bind.annotation.DeleteMapping; // Usado para mapear peticiones HTTP DELETE para desactivar médicos.
import org.springframework.web.bind.annotation.GetMapping; // Usado para mapear peticiones HTTP GET a métodos de consulta.
import org.springframework.web.bind.annotation.PathVariable; // Usado para extraer el identificador del médico desde la URL.
import org.springframework.web.bind.annotation.PostMapping; // Usado para mapear peticiones HTTP POST de registro.
import org.springframework.web.bind.annotation.PutMapping; // Usado para mapear peticiones HTTP PUT de actualización.
import org.springframework.web.bind.annotation.RequestBody; // Usado para vincular el cuerpo JSON con MedicoRequest.
import org.springframework.web.bind.annotation.RequestMapping; // Usado para definir la ruta base del recurso (/api/medicos).
import org.springframework.web.bind.annotation.RequestParam; // Usado para recibir filtros de búsqueda y paginación.
import org.springframework.web.bind.annotation.ResponseStatus; // Usado para definir el estado HTTP 204 NO_CONTENT al desactivar.
import org.springframework.web.bind.annotation.RestController; // Usado para marcar la clase como controlador REST.
import com.utp.clinitech.dto.MedicoRequest; // Usado para recibir datos de registro o edición de médicos.
import com.utp.clinitech.dto.MedicoResponse; // Usado para transferir los datos del médico en la respuesta.
import com.utp.clinitech.dto.PageResponse; // Usado para enviar listas paginadas de médicos.
import com.utp.clinitech.service.MedicoService; // Usado para ejecutar las operaciones de negocio de médicos.

// Controlador REST para la gestión del personal médico del hospital.
@RestController
@RequestMapping("/api/medicos")
public class MedicoController {
  private final MedicoService service;

  // Constructor con inyección del servicio de personal médico.
  public MedicoController(MedicoService service) {
    this.service = service;
  }

  // Endpoint para listar médicos filtrados según su especialidad médica.
  @GetMapping(params = "especialidadId")
  public List<MedicoResponse> porEspecialidad(@RequestParam Long especialidadId) {
    return service.porEspecialidad(especialidadId);
  }

  // Endpoint para búsqueda y listado paginado de médicos activos.
  @GetMapping
  public PageResponse<MedicoResponse> listar(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return service.listar(q, Math.max(0, page), Math.max(1, size));
  }

  // Endpoint administrativo para registrar un nuevo médico en la clínica.
  @PostMapping 
  public ResponseEntity<MedicoResponse> crear(@Valid @RequestBody MedicoRequest request) { 
    return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request)); 
  }

  // Endpoint administrativo para actualizar los datos o el horario de atención de un médico.
  @PutMapping("/{id}") 
  public MedicoResponse actualizar(@PathVariable Long id, @Valid @RequestBody MedicoRequest request) { 
    return service.actualizar(id, request); 
  }

  // Endpoint administrativo para dar de baja lógica a un médico.
  @DeleteMapping("/{id}") 
  @ResponseStatus(HttpStatus.NO_CONTENT) 
  public void desactivar(@PathVariable Long id) { 
    service.desactivar(id); 
  }
}
