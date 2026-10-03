package com.utp.clinitech.controller;

import jakarta.validation.Valid; // Usado para validar las solicitudes entrantes.
import java.util.List; // Usado para manejar listas de objetos.
import org.springframework.http.HttpStatus; // Usado para definir códigos de estado HTTP en las respuestas.
import org.springframework.http.ResponseEntity; // Usado para construir respuestas HTTP completas, incluyendo cuerpo y estado.
import org.springframework.web.bind.annotation.DeleteMapping; // Usado para mapear solicitudes HTTP DELETE a métodos de controlador.
import org.springframework.web.bind.annotation.GetMapping; // Usado para mapear solicitudes HTTP GET a métodos de controlador.
import org.springframework.web.bind.annotation.PathVariable; // Usado para extraer valores de variables de ruta en solicitudes HTTP.
import org.springframework.web.bind.annotation.PostMapping; // Usado para mapear solicitudes HTTP POST a métodos de controlador.
import org.springframework.web.bind.annotation.PutMapping; // Usado para mapear solicitudes HTTP PUT a métodos de controlador.
import org.springframework.web.bind.annotation.RequestBody; // Usado para vincular el cuerpo de la solicitud HTTP a un objeto Java.
import org.springframework.web.bind.annotation.RequestMapping; // Usado para definir la ruta base para todas las solicitudes manejadas por este controlador.
import org.springframework.web.bind.annotation.RequestParam; // Usado para extraer parámetros de consulta de solicitudes HTTP.
import org.springframework.web.bind.annotation.ResponseStatus; // Usado para definir el código de estado HTTP que se devolverá desde un método de controlador.
import org.springframework.web.bind.annotation.RestController; // Indica que esta clase es un controlador REST y que sus métodos devolverán datos directamente en el cuerpo de la respuesta.
import com.utp.clinitech.dto.PacienteRequest; // Usado para recibir datos de solicitud relacionados con pacientes.
import com.utp.clinitech.dto.PacienteResponse; // Usado para enviar datos de respuesta relacionados con pacientes.
import com.utp.clinitech.dto.PageResponse; // Usado para enviar respuestas paginadas.
import com.utp.clinitech.service.PacienteService; // Usado para manejar la lógica de negocio relacionada con pacientes.

@RestController // Indica que esta clase es un controlador REST.
@RequestMapping("/api/pacientes") // Define la ruta base para todas las solicitudes manejadas por este controlador.
public class PacienteController {
  private final PacienteService service; // Crea instancia de PacienteService para manejar la lógica de negocio relacionada con pacientes.

  // Constructor de la clase PacienteController que recibe una instancia de PacienteService.
  public PacienteController(PacienteService service) {
    this.service = service;
  }

  @GetMapping
  public PageResponse<PacienteResponse> listar(@RequestParam(defaultValue = "") String q,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { // Maneja solicitudes GET para listar pacientes con soporte de paginación y búsqueda.
    return service.listar(q, Math.max(0, page), Math.max(1, size)); // Retorna la respuesta paginada de pacientes obtenida del servicio, asegurando que los valores de página y tamaño sean válidos.
  }

  @GetMapping("/arbol/buscar/{dni}") // Define la ruta para buscar un paciente en el árbol por su DNI.
  public PacienteResponse buscarEnArbol(@PathVariable String dni) { 
    return service.buscarPorDniEnArbol(dni); // Retorna la respuesta del paciente encontrado en el árbol, o lanza una excepción si no se encuentra.
  }

  @GetMapping("/arbol/en-orden") // Define la ruta para listar todos los pacientes en orden según el árbol de búsqueda.
  public List<PacienteResponse> listarEnOrdenArbol() {
    return service.listarEnOrdenArbol(); // Retorna la lista de pacientes en orden según el árbol de búsqueda, filtrando solo los activos.
  }

  @GetMapping("/{id}") // Define la ruta para obtener un paciente por su ID.
  public PacienteResponse obtener(@PathVariable Long id) {
    return service.obtener(id); // Retorna la respuesta del paciente obtenido por su ID, o lanza una excepción si no se encuentra.
  }

  @PostMapping // Define la ruta para crear un nuevo paciente.
  public ResponseEntity<PacienteResponse> crear(@Valid @RequestBody PacienteRequest request) {
    PacienteResponse created = service.crear(request); // Llama al servicio para crear un nuevo paciente y obtiene la respuesta del paciente creado.
    return ResponseEntity.status(HttpStatus.CREATED).body(created); // Retorna una respuesta HTTP con el estado 201 (CREATED) y el cuerpo de la respuesta del paciente creado.
  }

  @PutMapping("/{id}") // Define la ruta para actualizar un paciente existente por su ID.
  public PacienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequest request) {
    return service.actualizar(id, request); // Retorna la respuesta del paciente actualizado, o lanza una excepción si no se encuentra.
  }

  @DeleteMapping("/{id}") // Define la ruta para desactivar un paciente existente por su ID.
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void desactivar(@PathVariable Long id) {
    service.desactivar(id); // Llama al servicio para desactivar el paciente por su ID. No retorna contenido en la respuesta.
  }
}
