package com.utp.clinitech.controller;

import java.time.LocalDate; // Fecha local para filtrar citas por día.
import java.time.OffsetDateTime; // Fecha y hora con zona horaria para agendar citas y mostrar disponibilidad.
import java.util.List; // Lista de citas o fechas disponibles.
import jakarta.validation.Valid; // Validación de datos de entrada en las solicitudes HTTP.
import org.springframework.http.HttpStatus; // Códigos de estado HTTP para las respuestas.
import org.springframework.http.ResponseEntity; // Representación de la respuesta HTTP completa, incluyendo el cuerpo y el estado.
import org.springframework.web.bind.annotation.GetMapping; // Mapeo de solicitudes HTTP GET a métodos del controlador.
import org.springframework.web.bind.annotation.PatchMapping; // Mapeo de solicitudes HTTP PATCH a métodos del controlador.
import org.springframework.web.bind.annotation.PathVariable; // Extracción de variables de ruta de la URL en los métodos del controlador.
import org.springframework.web.bind.annotation.PostMapping; // Mapeo de solicitudes HTTP POST a métodos del controlador.
import org.springframework.web.bind.annotation.RequestBody; // Extracción del cuerpo de la solicitud HTTP en los métodos del controlador.
import org.springframework.web.bind.annotation.RequestMapping; // Mapeo de la ruta base para todas las solicitudes del controlador.
import org.springframework.web.bind.annotation.RequestParam; // Extracción de parámetros de consulta de la URL en los métodos del controlador.
import org.springframework.web.bind.annotation.RestController; // Indica que la clase es un controlador REST.
import com.utp.clinitech.dto.CambiarEstadoCitaRequest; // DTO para cambiar el estado de una cita.
import com.utp.clinitech.dto.CitaRequest; // DTO para crear una nueva cita.
import com.utp.clinitech.dto.CitaResponse; // DTO para representar la respuesta de una cita.
import com.utp.clinitech.dto.ReprogramarCitaRequest; // DTO para reprogramar una cita.
import com.utp.clinitech.service.CitaService; // Servicio que contiene la lógica de negocio para manejar citas.

@RestController // Indica que la clase es un controlador REST y que sus métodos devolverán respuestas JSON.
@RequestMapping("/api/citas") // Define la ruta base para todas las solicitudes relacionadas con citas.
public class CitaController {
  private final CitaService service; // Crea una instancia del servicio de citas. 

  // Constructor que inyecta el servicio de citas en el controlador.
  public CitaController(CitaService service) {
    this.service = service;
  }

  @GetMapping // Mapea las solicitudes HTTP GET a este método para listar citas.
  public List<CitaResponse> listar(@RequestParam(required = false) Long pacienteId,
      @RequestParam(required = false) Long medicoId) {
    return service.listar(pacienteId, medicoId); // Retorna la lista de citas filtradas por paciente o médico según los parámetros proporcionados.
  }

  @GetMapping("/cola-atencion") // Define un endpoint para obtener la cola de atención de un médico en una fecha específica.
  public List<CitaResponse> colaAtencion(@RequestParam Long medicoId, @RequestParam(required = false) LocalDate fecha) {
    return service.colaAtencion(medicoId, fecha); // Retorna la lista de citas en la cola de atención para el médico y fecha especificados.
  }

  @GetMapping("/cola-atencion/siguiente") // Define un endpoint para obtener la siguiente cita en la cola de atención de un médico en una fecha específica.
  public CitaResponse siguienteEnCola(@RequestParam Long medicoId, @RequestParam(required = false) LocalDate fecha) {
    return service.siguienteEnCola(medicoId, fecha); // Retorna la siguiente cita en la cola de atención para el médico y fecha especificados.
  }

  @GetMapping("/disponibilidad") // Define un endpoint para obtener la disponibilidad de un médico en una fecha específica.
  public List<OffsetDateTime> disponibilidad(@RequestParam Long medicoId, @RequestParam LocalDate fecha) {
    return service.disponibilidad(medicoId, fecha); // Retorna la lista de horarios disponibles para el médico en la fecha especificada.
  }

  @PostMapping
  public ResponseEntity<CitaResponse> crear(@Valid @RequestBody CitaRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request)); // Crea una nueva cita y retorna la respuesta con el estado HTTP 201 (CREATED).
  }

  @PatchMapping("/{id}/reprogramar") // Define un endpoint para reprogramar una cita existente.
  public CitaResponse reprogramar(@PathVariable Long id, @Valid @RequestBody ReprogramarCitaRequest request) {
    return service.reprogramar(id, request); // Retorna la cita reprogramada después de actualizar su fecha y hora según la solicitud proporcionada.
  }

  @PatchMapping("/{id}/estado") // Define un endpoint para cambiar el estado de una cita existente.
  public CitaResponse estado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoCitaRequest request) {
    return service.cambiarEstado(id, request); // Retorna la cita con el estado actualizado después de cambiar su estado según la solicitud proporcionada.
  }
}
