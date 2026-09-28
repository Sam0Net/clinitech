package com.utp.clinitech.controller;
import java.time.*;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.utp.clinitech.dto.*;
import com.utp.clinitech.service.CitaService;
@RestController @RequestMapping("/api/citas")
public class CitaController {
  private final CitaService service;
  public CitaController(CitaService service) { this.service = service; }
  @GetMapping public List<CitaResponse> listar(@RequestParam(required = false) Long pacienteId, @RequestParam(required = false) Long medicoId) { return service.listar(pacienteId, medicoId); }
  @GetMapping("/cola-atencion") public List<CitaResponse> colaAtencion(@RequestParam Long medicoId, @RequestParam(required = false) LocalDate fecha) { return service.colaAtencion(medicoId, fecha); }
  @GetMapping("/cola-atencion/siguiente") public CitaResponse siguienteEnCola(@RequestParam Long medicoId, @RequestParam(required = false) LocalDate fecha) { return service.siguienteEnCola(medicoId, fecha); }
  @GetMapping("/disponibilidad") public List<OffsetDateTime> disponibilidad(@RequestParam Long medicoId, @RequestParam LocalDate fecha) { return service.disponibilidad(medicoId, fecha); }
  @PostMapping public ResponseEntity<CitaResponse> crear(@Valid @RequestBody CitaRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request)); }
  @PatchMapping("/{id}/reprogramar") public CitaResponse reprogramar(@PathVariable Long id, @Valid @RequestBody ReprogramarCitaRequest request) { return service.reprogramar(id, request); }
  @PatchMapping("/{id}/estado") public CitaResponse estado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoCitaRequest request) { return service.cambiarEstado(id, request); }
}
