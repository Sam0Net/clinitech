package com.utp.clinitech.controller;

import java.util.List; // Manejo de listas
import jakarta.validation.Valid; // Validación de datos de entrada
import org.springframework.http.HttpStatus; // Manejo de códigos de estado HTTP
import org.springframework.http.ResponseEntity; // Representación de respuestas HTTP
import org.springframework.web.bind.annotation.GetMapping; // Manejo de solicitudes GET
import org.springframework.web.bind.annotation.PathVariable; // Manejo de variables de ruta
import org.springframework.web.bind.annotation.PostMapping; // Manejo de solicitudes POST
import org.springframework.web.bind.annotation.RequestBody; // Manejo de cuerpos de solicitud
import org.springframework.web.bind.annotation.RequestMapping; // Manejo de rutas de solicitud
import org.springframework.web.bind.annotation.RequestParam; // Manejo de parámetros de solicitud
import org.springframework.web.bind.annotation.RestController; // Indica que esta clase es un controlador REST
import com.utp.clinitech.dto.EspecialidadRequest; // DTO para solicitudes de especialidad
import com.utp.clinitech.dto.EspecialidadResponse; // DTO para respuestas de especialidad
import com.utp.clinitech.service.EspecialidadService; // Servicio para manejar la lógica de negocio relacionada con especialidades

@RestController // Indica que esta clase es un controlador REST.
@RequestMapping("/api/especialidades") // Define la ruta base para todas las solicitudes.
public class EspecialidadController {
  private final EspecialidadService service; // Crea una instancia del servicio de especialidades.

  // Constructor que inyecta el servicio de especialidades.
  public EspecialidadController(EspecialidadService service) {
    this.service = service;
  }

  @GetMapping // Maneja solicitudes GET a la ruta base.
  public List<EspecialidadResponse> listar() {
    return service.listar(); // Llama al método listar del servicio para obtener todas las especialidades.
  }

  @GetMapping("/{id}/relacionadas") // Ruta para obtener especialidades relacionadas con una especialidad específica.
  public List<EspecialidadResponse> relacionadas(@PathVariable Long id) {
    return service.relacionadas(id); // Devuelve las especialidades relacionadas con la especialidad cuyo ID se pasa como parámetro de ruta.
  }

  @GetMapping("/interconsultas/ruta") // Ruta para obtener la ruta de interconsulta entre dos especialidades.
  public List<EspecialidadResponse> rutaInterconsulta(@RequestParam Long origenId, @RequestParam Long destinoId) {
    return service.rutaInterconsulta(origenId, destinoId); // Devolver la ruta de interconsulta entre las especialidades de origen y destino cuyos IDs se pasan como parámetros de solicitud.
  }

  @PostMapping // Maneja solicitudes POST a la ruta base.
  public ResponseEntity<EspecialidadResponse> crear(@Valid @RequestBody EspecialidadRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request)); // Crea una nueva especialidad a partir de los datos proporcionados en el cuerpo de la solicitud y devuelve la respuesta con un código de estado 201 (CREATED).
  }
}
