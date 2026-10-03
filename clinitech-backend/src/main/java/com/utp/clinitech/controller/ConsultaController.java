package com.utp.clinitech.controller;

import java.util.List; // Usado para retornar listas de consultas médicas en el historial clínico.
import jakarta.validation.Valid; // Usado para validar los campos del diagnóstico y tratamiento en la solicitud.
import org.springframework.http.HttpStatus; // Usado para definir el código de estado HTTP de retorno (ej. 201 CREATED).
import org.springframework.http.ResponseEntity; // Usado para estructurar la respuesta HTTP con código de estado y cuerpo.
import org.springframework.web.bind.annotation.GetMapping; // Usado para mapear peticiones HTTP GET a métodos de consulta.
import org.springframework.web.bind.annotation.PostMapping; // Usado para mapear peticiones HTTP POST a métodos de creación.
import org.springframework.web.bind.annotation.RequestBody; // Usado para enlazar el JSON del cuerpo de la petición con ConsultaRequest.
import org.springframework.web.bind.annotation.RequestMapping; // Usado para establecer la ruta base de consultas (/api/consultas).
import org.springframework.web.bind.annotation.RequestParam; // Usado para capturar el ID del paciente como parámetro de consulta.
import org.springframework.web.bind.annotation.RestController; // Usado para indicar que la clase es un controlador REST.
import com.utp.clinitech.dto.ConsultaRequest; // Usado para recibir los datos de la atención clínica realizada.
import com.utp.clinitech.dto.ConsultaResponse; // Usado para retornar los detalles de la consulta médica registrada.
import com.utp.clinitech.service.ConsultaService; // Usado para delegar la lógica de negocio de atención médica e historial.

// Controlador REST para la gestión de atenciones clínicas e historia médica de los pacientes.
@RestController 
@RequestMapping("/api/consultas")
public class ConsultaController {
  private final ConsultaService service;

  // Constructor que recibe el servicio de consultas médicas.
  public ConsultaController(ConsultaService service) { 
    this.service = service; 
  }

  // Endpoint para obtener el historial cronológico de consultas atendidas de un paciente.
  @GetMapping 
  public List<ConsultaResponse> historial(@RequestParam Long pacienteId) { 
    return service.historial(pacienteId); 
  }

  // Endpoint para que el médico registre el diagnóstico, receta y finalice la atención de una cita.
  @PostMapping 
  public ResponseEntity<ConsultaResponse> registrar(@Valid @RequestBody ConsultaRequest request) { 
    return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(request)); 
  }
}
